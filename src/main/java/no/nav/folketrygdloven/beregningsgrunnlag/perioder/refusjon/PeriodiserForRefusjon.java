package no.nav.folketrygdloven.beregningsgrunnlag.perioder.refusjon;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.NaturalYtelse;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.EksisterendeAndel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.RefusjonskravFrist;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.PeriodeModellRefusjon;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.PeriodiseringRefusjonProsesstruktur;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.IdentifisertePeriodeÅrsaker;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetAndel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(PeriodiserForRefusjon.ID)
public class PeriodiserForRefusjon extends LeafSpecification<PeriodiseringRefusjonProsesstruktur> {

	static final String ID = FastsettPerioderRefusjonRegel.ID + ".2";
	static final String BESKRIVELSE = "Periodiserer beregningsgrunnlaget ved gitte datoer og oppretter nye andeler.";

	public PeriodiserForRefusjon() {
		super(ID, BESKRIVELSE);
	}

	@Override
	public Evaluation evaluate(PeriodiseringRefusjonProsesstruktur prosesstruktur) {
		List<SplittetPeriode> splittetPerioder = periodiserBeregningsgrunnlag(prosesstruktur.getInput(), prosesstruktur.getIdentifisertePeriodeÅrsaker());
		prosesstruktur.setSplittetPerioder(splittetPerioder);
		SingleEvaluation resultat = ja();
		resultat.setEvaluationProperty("splittetPerioder", splittetPerioder);
		return resultat;
	}

	private static List<SplittetPeriode> periodiserBeregningsgrunnlag(PeriodeModellRefusjon input, IdentifisertePeriodeÅrsaker identifisertePeriodeÅrsaker) {
		// lag alle periodene, med riktige andeler
		Map<LocalDate, Set<PeriodeSplittData>> periodeMap = identifisertePeriodeÅrsaker.getPeriodeMap();

		List<Map.Entry<LocalDate, Set<PeriodeSplittData>>> entries = new ArrayList<>(periodeMap.entrySet());

		ListIterator<Map.Entry<LocalDate, Set<PeriodeSplittData>>> listIterator = entries.listIterator();

		List<SplittetPeriode> list = new ArrayList<>();
		while (listIterator.hasNext()) {
			Map.Entry<LocalDate, Set<PeriodeSplittData>> entry = listIterator.next();
			LocalDate periodeFom = entry.getKey();
			LocalDate periodeTom = utledPeriodeTom(entries, listIterator);
			Set<PeriodeSplittData> periodeSplittData = entry.getValue();

			List<EksisterendeAndel> førstePeriodeAndeler = input.getArbeidsforholdOgInntektsmeldinger().stream()
					.filter(im -> !im.erNyAktivitet())
					.map(im -> mapToArbeidsforhold(im, periodeFom))
					.toList();

			List<SplittetAndel> nyeAndeler = input.getArbeidsforholdOgInntektsmeldinger().stream()
					.filter(ArbeidsforholdOgInntektsmelding::erNyAktivitet)
					.filter(im -> !im.slutterFørSkjæringstidspunkt(input.getSkjæringstidspunkt()))
					.filter(im -> harRefusjonskravIPeriode(im, periodeFom))
					.map(im -> mapSplittetAndel(im, periodeFom))
					.toList();

			Periode periode = new Periode(periodeFom, periodeTom);
			SplittetPeriode splittetPeriode = SplittetPeriode.builder()
					.medPeriode(periode)
					.medPeriodeÅrsaker(getPeriodeÅrsaker(periodeSplittData, input.getSkjæringstidspunkt(), periodeFom))
					.medFørstePeriodeAndeler(førstePeriodeAndeler)
					.medNyeAndeler(nyeAndeler)
					.build();
			list.add(splittetPeriode);
		}
		return list;
	}


	private static LocalDate utledPeriodeTom(List<Map.Entry<LocalDate, Set<PeriodeSplittData>>> entries, ListIterator<Map.Entry<LocalDate, Set<PeriodeSplittData>>> listIterator) {
		return listIterator.hasNext() ?
				entries.get(listIterator.nextIndex()).getKey().minusDays(1) :
				null;
	}

	private static boolean harRefusjonskravIPeriode(ArbeidsforholdOgInntektsmelding im, LocalDate periodeFom) {
		// Mapper inn alle krav for å kunne sette utfall
		return im.getGyldigeRefusjonskrav().stream()
				.anyMatch(refusjonskrav -> refusjonskrav.getMånedsbeløp().compareTo(BigDecimal.ZERO) > 0 &&
						refusjonskrav.getPeriode().inneholder(periodeFom));
	}


	private static SplittetAndel mapSplittetAndel(ArbeidsforholdOgInntektsmelding im, LocalDate periodeFom) {
		Optional<Refusjonskrav> kravForPeriode = im.getGyldigeRefusjonskrav().stream()
				.filter(refusjonskrav -> refusjonskrav.getPeriode().inneholder(periodeFom))
				.findFirst();
		BigDecimal innvilgetRefusjonPrÅr = kravForPeriode
				.map(refusjonskrav -> refusjonskrav.getInnvilgetBeløp().multiply(BigDecimal.valueOf(12)))
				.orElse(BigDecimal.ZERO);
		BigDecimal kravPrÅr = kravForPeriode
				.map(refusjonskrav -> refusjonskrav.getMånedsbeløp().multiply(BigDecimal.valueOf(12)))
				.orElse(BigDecimal.ZERO);

		Periode ansettelsesPeriode = im.getAnsettelsesperiode();

		SplittetAndel.Builder builder = SplittetAndel.builder()
				.medAktivitetstatus(im.getAktivitetStatus())
				.medArbeidsforhold(im.getArbeidsforhold())
				.medRefusjonskravPrÅr(kravPrÅr)
				.medInnvilgetRefusjonskravPrÅr(innvilgetRefusjonPrÅr)
				.medRefusjonskravFristUtfall(kravForPeriode.map(Refusjonskrav::getFristvurdering).orElse(null))
				.medAnvendtRefusjonskravfristHjemmel(im.getRefusjonskravFrist().map(RefusjonskravFrist::getAnvendtHjemmel).orElse(null));
		settAnsettelsesPeriodeHvisFinnes(ansettelsesPeriode, builder);
		return builder.build();
	}

	private static void settAnsettelsesPeriodeHvisFinnes(Periode ansettelsesPeriode, SplittetAndel.Builder builder) {
		if (ansettelsesPeriode != null) {
			builder
					.medArbeidsperiodeFom(ansettelsesPeriode.getFom())
					.medArbeidsperiodeTom(ansettelsesPeriode.getTom());
		}
	}


	private static EksisterendeAndel mapToArbeidsforhold(ArbeidsforholdOgInntektsmelding im, LocalDate fom) {
		Optional<Refusjonskrav> kravForPeriode = im.getGyldigeRefusjonskrav().stream()
				.filter(refusjon -> refusjon.getPeriode().inneholder(fom))
				.findFirst();
		Optional<BigDecimal> refusjonskravPrÅr = kravForPeriode
				.map(refusjonskrav -> refusjonskrav.getMånedsbeløp().multiply(BigDecimal.valueOf(12)));
		Optional<BigDecimal> innvilgetRefusjonPrÅr = kravForPeriode
				.map(refusjonskrav -> refusjonskrav.getInnvilgetBeløp().multiply(BigDecimal.valueOf(12)));
		Optional<BigDecimal> naturalytelseBortfaltPrÅr = im.getNaturalYtelser().stream()
				.filter(naturalYtelse -> naturalYtelse.getFom().isEqual(DateUtil.TIDENES_BEGYNNELSE))
				.filter(naturalYtelse -> naturalYtelse.getTom().isBefore(fom))
				.map(NaturalYtelse::getBeløp)
				.reduce(BigDecimal::add);
		Optional<BigDecimal> naturalytelseTilkommer = im.getNaturalYtelser().stream()
				.filter(naturalYtelse -> naturalYtelse.getTom().isEqual(DateUtil.TIDENES_ENDE))
				.filter(naturalYtelse -> naturalYtelse.getFom().isBefore(fom))
				.map(NaturalYtelse::getBeløp)
				.reduce(BigDecimal::add);
		return EksisterendeAndel.builder()
				.medAndelNr(im.getAndelsnr())
				.medRefusjonskravPrÅr(refusjonskravPrÅr.orElse(null))
				.medInnvilgetRefusjonPrÅr(innvilgetRefusjonPrÅr.orElse(BigDecimal.ZERO))
				.medNaturalytelseTilkommetPrÅr(naturalytelseTilkommer.orElse(null))
				.medNaturalytelseBortfaltPrÅr(naturalytelseBortfaltPrÅr.orElse(null))
				.medArbeidsforhold(im.getArbeidsforhold())
				.medRefusjonskravFristVurdering(kravForPeriode.map(Refusjonskrav::getFristvurdering).orElse(null))
				.medAnvendtRefusjonskravfristHjemmel(im.getRefusjonskravFrist().map(RefusjonskravFrist::getAnvendtHjemmel).orElse(null))
				.build();
	}

	private static List<PeriodeÅrsak> getPeriodeÅrsaker(Set<PeriodeSplittData> periodeSplittData, LocalDate skjæringstidspunkt, LocalDate periodeFom) {
		return periodeSplittData.stream()
				.map(PeriodeSplittData::getPeriodeÅrsak)
				.filter(periodeÅrsak -> !PeriodeÅrsak.UDEFINERT.equals(periodeÅrsak))
				.filter(periodeÅrsak -> !skjæringstidspunkt.equals(periodeFom))
				.toList();
	}

}
