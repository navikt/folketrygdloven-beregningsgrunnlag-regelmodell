package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.dagpengerelleraap;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmBGForDagpengerFastsattManuelt.ID)
class SjekkOmBGForDagpengerFastsattManuelt extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR_10.4";
    static final String BESKRIVELSE = "Er beregnngsgrunnlag for dagpenger fastsatt manuelt? ";

    SjekkOmBGForDagpengerFastsattManuelt() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        var dagpengerStatus = grunnlag.getBeregningsgrunnlagFraDagpenger();
        boolean manueltFastsattDagpenger = dagpengerStatus.map(BeregningsgrunnlagPrStatus::erFastsattAvSaksbehandler).orElse(false);
	    return manueltFastsattDagpenger ? ja() : nei();
    }
}
