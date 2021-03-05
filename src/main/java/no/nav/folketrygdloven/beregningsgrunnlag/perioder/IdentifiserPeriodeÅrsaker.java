package no.nav.folketrygdloven.beregningsgrunnlag.perioder;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModellGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModellRefusjonOgNaturalytelse;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModellUtbetalingsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeSplittProsesstruktur;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.IdentifisertePeriodeÅrsaker;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(IdentifiserPeriodeÅrsaker.ID)
public class IdentifiserPeriodeÅrsaker extends LeafSpecification<PeriodeSplittProsesstruktur> {

    static final String ID = FastsettPeriodeRegel.ID + ".1";
    static final String BESKRIVELSE = "Identifiserer dato og årsak for splitting";

    public IdentifiserPeriodeÅrsaker() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(PeriodeSplittProsesstruktur prosseseringStruktur) {
        Map<String, Object> resultater = new HashMap<>();
        IdentifisertePeriodeÅrsaker årsaker = identifiser(prosseseringStruktur.getInput(), resultater);
        prosseseringStruktur.setIdentifisertePeriodeÅrsaker(årsaker);
        SingleEvaluation resultat = ja();
        resultat.setEvaluationProperties(resultater);
        return resultat;
    }

    static IdentifisertePeriodeÅrsaker identifiser(PeriodeModell input, Map<String, Object> resultater) {
        LocalDate skjæringstidspunkt = input.getSkjæringstidspunkt();
        IdentifisertePeriodeÅrsaker map = new IdentifisertePeriodeÅrsaker();
        leggTilPeriodesplitterForEksisterendePerioder(input, map);
        resultater.put("eksisterendePerioder", map.getPeriodeMap());

        if (input instanceof PeriodeModellRefusjonOgNaturalytelse) {
	        input.getEndringListeForSplitting().forEach(inntektsmelding -> {
		        resultater.put("refusjonForArbeidsforhold", inntektsmelding.getArbeidsforhold());
		        Set<PeriodeSplittData> refusjonPerioder = IdentifiserPerioderForRefusjon.identifiserPerioderForRefusjon((ArbeidsforholdOgInntektsmelding) inntektsmelding, resultater);
		        refusjonPerioder.forEach(map::leggTilPeriodeÅrsak);
	        });

	        Map<ArbeidsforholdOgInntektsmelding, List<Refusjonskrav>> refusjonskravPrArbeidsgiver = GrupperPeriodeÅrsakerPerArbeidsgiver.grupper(map.getPeriodeMap());
	        input.getEndringListeForSplitting().forEach(endring -> {
		        ArbeidsforholdOgInntektsmelding inntektsmelding = (ArbeidsforholdOgInntektsmelding) endring;
		        List<Refusjonskrav> gyldigeRefusjonskrav = refusjonskravPrArbeidsgiver.getOrDefault(inntektsmelding, List.of());
		        resultater.put("gyldigeRefusjonskrav", gyldigeRefusjonskrav);
		        (inntektsmelding).setGyldigeRefusjonskrav(gyldigeRefusjonskrav);
	        });

	        input.getEndringListeForSplitting().forEach(inntektsmelding -> {
		        resultater.put("arbeidsforhold", inntektsmelding.getArbeidsforhold());
		        Set<PeriodeSplittData> naturalYtelsePerioder = IdentifiserPerioderForNaturalytelse.identifiserPerioderForNaturalytelse((ArbeidsforholdOgInntektsmelding) inntektsmelding, skjæringstidspunkt);
		        naturalYtelsePerioder.forEach(map::leggTilPeriodeÅrsak);
		        resultater.put("naturalYtelsePerioder", naturalYtelsePerioder);
	        });
        } else if (input instanceof PeriodeModellGradering) {
		    input.getEndringListeForSplitting().forEach(andelGradering -> {
			    resultater.put("graderingForAktivitetstatus", andelGradering.getAktivitetStatus());
			    resultater.put("graderingForArbeidsforhold", andelGradering.getArbeidsforhold());
			    Set<PeriodeSplittData> graderingPerioder = IdentifiserPerioderForGradering.identifiser(input, andelGradering);
			    graderingPerioder.forEach(map::leggTilPeriodeÅrsak);
			    resultater.put("graderingPerioder", graderingPerioder);
		    });
	    } else if (input instanceof PeriodeModellUtbetalingsgrad) {
		    input.getEndringListeForSplitting().forEach(endringISøktYtelse -> {
			    resultater.put("aktivitet", endringISøktYtelse.getArbeidsforhold());
			    Set<PeriodeSplittData> endringerISøktYtelse = IdentifiserPerioderForEndringIUtbetalingsgrad.identifiser(endringISøktYtelse);
			    endringerISøktYtelse.forEach(map::leggTilPeriodeÅrsak);
			    resultater.put("endringerISøktYtelse", endringerISøktYtelse);
		    });
	    }

        // må alltid ha en første periode, også når ingen gradering/refusjon/naturalytelse fra start
        if (!map.getPeriodeMap().containsKey(input.getSkjæringstidspunkt())) {
            PeriodeSplittData førstePeriode = PeriodeSplittData.builder()
                .medFom(input.getSkjæringstidspunkt())
                .medPeriodeÅrsak(PeriodeÅrsak.UDEFINERT)
                .build();
            map.leggTilPeriodeÅrsak(førstePeriode);
        }
        return map;
    }

    private static void leggTilPeriodesplitterForEksisterendePerioder(PeriodeModell input, IdentifisertePeriodeÅrsaker map) {
        input.getEksisterendePerioder().forEach(eksisterendePeriode -> {
            if (!eksisterendePeriode.getPeriodeÅrsaker().isEmpty()) {
                eksisterendePeriode.getPeriodeÅrsaker().forEach(periodeÅrsak -> {
                    PeriodeSplittData periodeSplittData = PeriodeSplittData.builder()
                        .medFom(eksisterendePeriode.getPeriode().getFom())
                        .medPeriodeÅrsak(periodeÅrsak).build();
                    map.leggTilPeriodeÅrsak(periodeSplittData);
                });
            }
        });
    }
}
