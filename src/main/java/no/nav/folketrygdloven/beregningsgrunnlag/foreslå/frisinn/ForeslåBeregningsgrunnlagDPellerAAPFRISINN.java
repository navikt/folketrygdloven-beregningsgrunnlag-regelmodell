package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import static no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil.TIDENES_ENDE;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.InntektPeriodeType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.util.Virkedager;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(ForeslåBeregningsgrunnlagDPellerAAPFRISINN.ID)
class ForeslåBeregningsgrunnlagDPellerAAPFRISINN extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR_10.1";
    static final String BESKRIVELSE = "Foreslå beregningsgrunnlag for Dagpenger/AAP";

    ForeslåBeregningsgrunnlagDPellerAAPFRISINN() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus bgPerStatus = grunnlag.getBeregningsgrunnlagPrStatus().stream()
            .filter(bgps -> bgps.getAktivitetStatus().erAAPellerDP())
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Ingen aktivitetstatus av type DP eller AAP funnet."));
        Periode beregningsgrunnlagPeriode = grunnlag.getBeregningsgrunnlagPeriode();
        if (beregningsgrunnlagPeriode.getTom().isEqual(TIDENES_ENDE)) {
            BigDecimal beregnetPrÅr = BigDecimal.ZERO;
            Map<String, Object> resultater = settBeregnetOgHjemmel(grunnlag, bgPerStatus, beregnetPrÅr, 0L);
            return beregnet(resultater);
        }
        List<Periodeinntekt> overlappendeMeldkortListe = grunnlag.getInntektsgrunnlag().getPeriodeinntekter().stream()
            .filter(pi -> pi.getInntektskilde().equals(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP))
            .filter(pi -> Periode.of(pi.getFom(), pi.getTom()).overlapper(beregningsgrunnlagPeriode))
            .collect(Collectors.toList());
        BigDecimal totalInntektFraMeldekortIPeriode = overlappendeMeldkortListe.stream()
            .map(pi -> {
                var overlappendePeriodeFom = pi.getFom().isBefore(beregningsgrunnlagPeriode.getFom()) ? beregningsgrunnlagPeriode.getFom() : pi.getFom();
                var overlappendePeriodeTom = pi.getTom().isAfter(beregningsgrunnlagPeriode.getTom()) ? beregningsgrunnlagPeriode.getTom() : pi.getTom();
                BigDecimal utbetalingsFaktor = pi.getUtbetalingsgrad()
                    .orElseThrow(() -> new IllegalStateException("Utbetalingsgrad for DP/AAP mangler."))
                    .divide(BigDecimal.valueOf(200), 10, RoundingMode.HALF_UP);
                if (!pi.getInntektPeriodeType().equals(InntektPeriodeType.DAGLIG)) {
                    throw new IllegalStateException("Forventer inntekter med dagsats");
                }
                return BigDecimal.valueOf(Virkedager.beregnAntallVirkedagerEllerKunHelg(overlappendePeriodeFom, overlappendePeriodeTom))
                    .multiply(pi.getInntekt())
                    .multiply(utbetalingsFaktor);
            })
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);

        int virkedagerIPeriode = Virkedager.beregnAntallVirkedager(beregningsgrunnlagPeriode);
        BigDecimal originalDagsats = virkedagerIPeriode == 0 ? BigDecimal.ZERO :
            totalInntektFraMeldekortIPeriode.divide(BigDecimal.valueOf(virkedagerIPeriode), 10, RoundingMode.HALF_EVEN);
        BigDecimal beregnetPrÅr = originalDagsats.multiply(BigDecimal.valueOf(260));
        Map<String, Object> resultater = settBeregnetOgHjemmel(grunnlag, bgPerStatus, beregnetPrÅr, originalDagsats.longValue());
        return beregnet(resultater);
    }

    private Map<String, Object> settBeregnetOgHjemmel(BeregningsgrunnlagPeriode grunnlag, BeregningsgrunnlagPrStatus bgPerStatus, BigDecimal beregnetPrÅr, long l) {
        BeregningsgrunnlagPrStatus.builder(bgPerStatus)
            .medBeregnetPrÅr(beregnetPrÅr)
            .medÅrsbeløpFraTilstøtendeYtelse(beregnetPrÅr)
            .medOrginalDagsatsFraTilstøtendeYtelse(l)
            .build();
        BeregningsgrunnlagHjemmel hjemmel = BeregningsgrunnlagHjemmel.KORONALOVEN_3;
        grunnlag.getBeregningsgrunnlag().getAktivitetStatus(bgPerStatus.getAktivitetStatus()).setHjemmel(hjemmel);
        Map<String, Object> resultater = new HashMap<>();
        resultater.put("beregnetPrÅr." + bgPerStatus.getAktivitetStatus().name(), beregnetPrÅr);
        resultater.put("tilstøtendeYtelserPrÅr." + bgPerStatus.getAktivitetStatus().name(), beregnetPrÅr);
        resultater.put("hjemmel", hjemmel);
        return resultater;
    }
}
