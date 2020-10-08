package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.dagpengerelleraap;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmBGForAAPFastsattManuelt.ID)
class SjekkOmBGForAAPFastsattManuelt extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR_10.5";
    static final String BESKRIVELSE = "Er beregnngsgrunnlag for aap fastsatt manuelt? ";

    SjekkOmBGForAAPFastsattManuelt() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus aapStatus = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.AAP);
        boolean manueltFastsattAAP = aapStatus != null && aapStatus.erFastsattAvSaksbehandler();
        return manueltFastsattAAP ? ja() : nei();
    }


}
