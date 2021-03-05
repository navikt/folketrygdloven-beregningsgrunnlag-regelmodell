package no.nav.folketrygdloven.beregningsgrunnlag.perioder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.BruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModellRefusjonOgNaturalytelse;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodisertBruttoBeregningsgrunnlag;

class ErTotaltRefusjonskravStørreEnnEllerLikSeksG {
	private ErTotaltRefusjonskravStørreEnnEllerLikSeksG() {
		// skjul public constructor
	}

	public static boolean vurder(PeriodeModell input, LocalDate dato) {
		BigDecimal grunnbeløp = input.getGrunnbeløp();
		BigDecimal seksG = grunnbeløp.multiply(BigDecimal.valueOf(6));
		BigDecimal totaltRefusjonskravPrÅr = beregnTotaltRefusjonskravPrÅrPåDato(input.getPeriodisertBruttoBeregningsgrunnlagList(), dato);
		return totaltRefusjonskravPrÅr.compareTo(seksG) >= 0;
	}

	private static BigDecimal beregnTotaltRefusjonskravPrÅrPåDato(List<PeriodisertBruttoBeregningsgrunnlag> bgPerioder, LocalDate dato) {
		BigDecimal årsbeløp = bgPerioder.stream()
				.filter(bgPeriode -> bgPeriode.getPeriode().inneholder(dato))
				.flatMap(periode -> periode.getBruttoBeregningsgrunnlag().stream())
				.map(BruttoBeregningsgrunnlag::getRefusjonPrÅr)
				.filter(Objects::nonNull)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		return årsbeløp;
	}
}
