package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.fp;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;

public class ForeldrepengerGrunnlag extends YtelsesSpesifiktGrunnlag {
    private boolean erBesteberegnet;

    public ForeldrepengerGrunnlag(boolean erBesteberegnet) {
        super("FP");
        this.erBesteberegnet = erBesteberegnet;
    }

    public boolean erBesteberegnet() {
        return erBesteberegnet;
    }

    @Override
    public BigDecimal finnAndelAvBeregnet(BigDecimal beregnetPrÅr, BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, BeregningsgrunnlagPeriode periode) {
        return beregnetPrÅr;
    }
}
