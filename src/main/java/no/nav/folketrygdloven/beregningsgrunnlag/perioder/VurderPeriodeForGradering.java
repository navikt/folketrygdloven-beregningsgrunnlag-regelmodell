package no.nav.folketrygdloven.beregningsgrunnlag.perioder;

import static no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil.TIDENES_ENDE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AndelEndring;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.BruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodisertBruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

class VurderPeriodeForGradering {
	private VurderPeriodeForGradering() {
		// skjul public constructor
	}

	static List<PeriodeSplittData> vurder(PeriodeModell input, AndelEndring andelGradering, Periode gradering) {
		LocalDateTimeline<Object> avkortingGrunnetRefusjonTidslinje = lagTidslinjeForAvkortingGrunnetRefusjon(input, andelGradering, gradering);
		if (!avkortingGrunnetRefusjonTidslinje.isEmpty()) {
			return avkortingGrunnetRefusjonTidslinje.getDatoIntervaller().stream()
					.flatMap(VurderPeriodeForGradering::lagSplittFraSegment)
					.collect(Collectors.toList());
		}

		LocalDateTimeline<Object> tidslinjeUtenInntekt = lagTidslinjeUtenInntekt(input, andelGradering, gradering);
		if (!tidslinjeUtenInntekt.isEmpty()) {
			return tidslinjeUtenInntekt.getDatoIntervaller().stream()
					.flatMap(VurderPeriodeForGradering::lagSplittFraSegment)
					.collect(Collectors.toList());
		}

		LocalDateTimeline<Object> tidslinjeForAvkortingGrunnetAnnenInntekt = lagTidslinjeForAvkortetGrunnetHøyerePrioriterteAndeler(input, andelGradering, gradering);
		if (!tidslinjeForAvkortingGrunnetAnnenInntekt.isEmpty()) {
			return tidslinjeForAvkortingGrunnetAnnenInntekt.getDatoIntervaller().stream()
					.flatMap(VurderPeriodeForGradering::lagSplittFraSegment)
					.collect(Collectors.toList());
		}

		return Collections.emptyList();
	}

	private static LocalDateTimeline<Object> lagTidslinjeForAvkortingGrunnetRefusjon(PeriodeModell input, AndelEndring andelGradering, Periode gradering) {
		var perioderMedTotalOver6G = input.getPeriodisertBruttoBeregningsgrunnlagList().stream()
				.map(PeriodisertBruttoBeregningsgrunnlag::getPeriode)
				.filter(periode -> andelGradering.getGraderinger().stream().anyMatch(g -> g.getPeriode().overlapper(periode)))
				.filter(periode -> ErTotaltRefusjonskravStørreEnnEllerLikSeksG.vurder(input, periode.getFom()))
				.map(p -> LocalDateSegment.emptySegment(p.getFom(), p.getTom()))
				.collect(Collectors.toList());
		var perioderUtenRefusjonForAndel = input.getPeriodisertBruttoBeregningsgrunnlagList().stream()
				.filter(p -> p.getPeriode().overlapper(gradering))
				.filter(p -> p.getBruttoBeregningsgrunnlag().stream().noneMatch(andel ->
						matcherGraderingOgAndel(andelGradering, andel)
								&& andel.getRefusjonPrÅr() != null && andel.getRefusjonPrÅr().compareTo(BigDecimal.ZERO) > 0))
				.map(PeriodisertBruttoBeregningsgrunnlag::getPeriode)
				.map(p -> LocalDateSegment.emptySegment(p.getFom(), p.getTom()))
				.collect(Collectors.toList());

		LocalDateTimeline<Object> timeline = lagTidslinjeForGradering(gradering.getFom(), gradering.getTom());
		timeline = timeline.intersection(new LocalDateTimeline<>(perioderMedTotalOver6G));
		timeline = timeline.intersection(new LocalDateTimeline<>(perioderUtenRefusjonForAndel));

		return timeline;
	}

	private static LocalDateTimeline<Object> lagTidslinjeUtenInntekt(PeriodeModell input, AndelEndring andelGradering, Periode gradering) {
		LocalDateTimeline<Object> timeline = lagTidslinjeForGradering(gradering.getFom(), gradering.getTom());
		var perioderUtenInntekt = input.getPeriodisertBruttoBeregningsgrunnlagList().stream()
				.filter(p -> p.getPeriode().overlapper(gradering))
				.filter(p -> p.getBruttoBeregningsgrunnlag().stream().allMatch(andel ->
						!matcherGraderingOgAndel(andelGradering, andel)
								|| ((andel.getBruttoPrÅr() == null || andel.getBruttoPrÅr().compareTo(BigDecimal.ZERO) == 0)
				&& (andel.getRefusjonPrÅr() == null || andel.getRefusjonPrÅr().compareTo(BigDecimal.ZERO) == 0))))
				.map(PeriodisertBruttoBeregningsgrunnlag::getPeriode)
				.map(p -> LocalDateSegment.emptySegment(p.getFom(), p.getTom()))
				.collect(Collectors.toList());
		timeline = timeline.intersection(new LocalDateTimeline<>(perioderUtenInntekt));
		return timeline;
	}

	private static LocalDateTimeline<Object> lagTidslinjeForAvkortetGrunnetHøyerePrioriterteAndeler(PeriodeModell input, AndelEndring andelGradering, Periode gradering) {
		LocalDateTimeline<Object> timeline = lagTidslinjeForGradering(TIDENES_ENDE, gradering.getTom());
		var avkortetAvHøyerePrioritet = input.getPeriodisertBruttoBeregningsgrunnlagList().stream()
				.filter(periodisertBg -> periodisertBg.getPeriode().overlapper(gradering))
				.filter(periodisertBg -> ErHøyerePrioriterteAndelerBruttoMinst6G.vurder(input.getGrunnbeløp(), periodisertBg, andelGradering))
				.map(PeriodisertBruttoBeregningsgrunnlag::getPeriode)
				.map(p -> LocalDateSegment.emptySegment(p.getFom(), p.getTom()))
				.collect(Collectors.toList());
		timeline = timeline.intersection(new LocalDateTimeline<>(avkortetAvHøyerePrioritet));
		return timeline;
	}

	private static LocalDateTimeline<Object> lagTidslinjeForGradering(LocalDate fomDato, LocalDate tomDato) {
		var graderinger = List.of(LocalDateSegment.emptySegment(fomDato, tomDato == null ? TIDENES_ENDE : tomDato));
		return new LocalDateTimeline<>(graderinger);
	}

	private static Stream<? extends PeriodeSplittData> lagSplittFraSegment(LocalDateInterval p) {
		PeriodeSplittData periodeSplitt = lagSplittForStartAvGradering(p.getFomDato());
		if (p.getTomDato() != null && !LocalDateInterval.TIDENES_ENDE.equals(p.getTomDato())) {
			PeriodeSplittData opphørGraderingSplitt = lagSplittForOpphørAvGradering(p.getTomDato());
			return Stream.of(periodeSplitt, opphørGraderingSplitt);
		}
		return Stream.of(periodeSplitt);
	}

	private static PeriodeSplittData lagSplittForStartAvGradering(LocalDate fom) {
		return PeriodeSplittData.builder()
				.medPeriodeÅrsak(PeriodeÅrsak.GRADERING)
				.medFom(fom)
				.build();
	}

	private static PeriodeSplittData lagSplittForOpphørAvGradering(LocalDate graderingTom) {
		return PeriodeSplittData.builder()
				.medPeriodeÅrsak(PeriodeÅrsak.GRADERING_OPPHØRER)
				.medFom(graderingTom.plusDays(1))
				.build();
	}

	private static boolean matcherGraderingOgAndel(AndelEndring andelGradering, BruttoBeregningsgrunnlag andel) {
		return andel.getAktivitetStatus().equals(andelGradering.getAktivitetStatus()) &&
				(andelGradering.getArbeidsforhold() == null ||
						andel.getArbeidsforhold().map(arbeidsforhold -> arbeidsforhold.getArbeidsgiverId().equals(andelGradering.getArbeidsforhold().getArbeidsgiverId())
								&& (arbeidsforhold.getArbeidsforholdId() == null
								|| andelGradering.getArbeidsforhold().getArbeidsforholdId() == null
								|| arbeidsforhold.getArbeidsforholdId().equals(andelGradering.getArbeidsforhold().getArbeidsforholdId()))).orElse(true));
	}
}
