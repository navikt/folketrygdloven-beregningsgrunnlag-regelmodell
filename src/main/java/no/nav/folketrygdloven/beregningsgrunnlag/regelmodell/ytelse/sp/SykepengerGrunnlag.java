package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.sp;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;

public class SykepengerGrunnlag extends YtelsesSpesifiktGrunnlag {

    public SykepengerGrunnlag() {
        super("SP");
    }

    @Override
    public BigDecimal finnAndelAvBeregnet(BigDecimal beregnetPrÅr, BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, BeregningsgrunnlagPeriode periode) {
        return beregnetPrÅr;
    }
}
