package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp.OmsorgspengerGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SkalGjøreAvviksvurdering.ID)
class SkalGjøreAvviksvurdering extends LeafSpecification<BeregningsgrunnlagPeriode> {
    static final String ID = "FP_BR 26.1";
    static final String BESKRIVELSE = "Skal det gjøres avviksvurdering etter § 8-30?";

    public SkalGjøreAvviksvurdering() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        if(skalAlltidSetteAksjonspunkt(grunnlag)){
            return ja();
        }
        OmsorgspengerGrunnlag ompGrunnlag = (OmsorgspengerGrunnlag) grunnlag.getBeregningsgrunnlag().getYtelsesSpesifiktGrunnlag();
        return ompGrunnlag.erDirekteUtbetalingPåSkjæringstidspunktet() ? ja() : nei();
    }

    private boolean skalAlltidSetteAksjonspunkt(BeregningsgrunnlagPeriode grunnlag){
        return !grunnlag.skalSjekkeRefusjonFørAvviksvurdering() || erFrilanser(grunnlag);
    }

    private boolean erFrilanser(BeregningsgrunnlagPeriode grunnlag){
        return grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getFrilansArbeidsforhold().isPresent();
    }
}

