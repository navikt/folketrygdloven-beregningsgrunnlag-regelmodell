package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelModell;

import java.math.BigDecimal;
import java.math.RoundingMode;

class OmfordelNaturalytelseForArbeidsforhold extends OmfordelForArbeidsforhold {

    OmfordelNaturalytelseForArbeidsforhold(FordelModell beregningsgrunnlagPeriode) {
        super(beregningsgrunnlagPeriode);
    }

    @Override
    protected void flyttFraAktivitet(FordelAndelModell arbeidMedFlyttbartGrunnlag, BigDecimal beløpSomSkalFlyttes) {
        BigDecimal bortfaltPrÅr = arbeidMedFlyttbartGrunnlag.getGradertNaturalytelseBortfaltPrÅr().orElse(BigDecimal.ZERO);
        FordelAndelModell.oppdater(arbeidMedFlyttbartGrunnlag).medNaturalytelseBortfaltPrÅr(
				skalerOpp(bortfaltPrÅr.subtract(beløpSomSkalFlyttes), arbeidMedFlyttbartGrunnlag.getUtbetalingsgrad()));
    }

	private static BigDecimal skalerOpp(BigDecimal nyttFordeltBeløp, BigDecimal utbetalingsgrad) {
		return nyttFordeltBeløp.multiply(BigDecimal.valueOf(100).divide(utbetalingsgrad, 10, RoundingMode.HALF_UP));
	}

    @Override
    protected BigDecimal finnFlyttbartBeløp(FordelAndelModell arbeidMedOmfordelbartBg) {
        BigDecimal naturalytelseBortfaltPrÅr = arbeidMedOmfordelbartBg.getGradertNaturalytelseBortfaltPrÅr().orElse(BigDecimal.ZERO);
        BigDecimal refusjonskrav = arbeidMedOmfordelbartBg.getGradertRefusjonPrÅr().orElse(BigDecimal.ZERO);
        return naturalytelseBortfaltPrÅr.subtract(refusjonskrav).max(BigDecimal.ZERO);
    }

}
