package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;

public class OmsorgspengerGrunnlag extends YtelsesSpesifiktGrunnlag {

    private BigDecimal gradertRefusjonVedSkjæringstidspunkt;

    public OmsorgspengerGrunnlag(BigDecimal gradertRefusjonVedSkjæringstidspunkt) {
        super("OMP");
        this.gradertRefusjonVedSkjæringstidspunkt = gradertRefusjonVedSkjæringstidspunkt;
    }

    public BigDecimal getGradertRefusjonVedSkjæringstidspunkt() {
        return gradertRefusjonVedSkjæringstidspunkt;
    }

    public boolean erDirekteUtbetalingPåSkjæringstidspunktet() {
        BeregningsgrunnlagPeriode førstePeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BigDecimal minsteRefusjon = førstePeriode.getGrenseverdi().min(this.getGradertRefusjonVedSkjæringstidspunkt());
        BigDecimal totaltBeregningsgrunnlag = førstePeriode.getBeregningsgrunnlagPrStatus().stream()
            .map(BeregningsgrunnlagPrStatus::getGradertBruttoPrÅr)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avkortetTotaltGrunnlag = førstePeriode.getGrenseverdi().min(totaltBeregningsgrunnlag);
        return minsteRefusjon.compareTo(avkortetTotaltGrunnlag) < 0;
    }

}
