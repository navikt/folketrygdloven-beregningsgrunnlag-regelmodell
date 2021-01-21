package no.nav.folketrygdloven.beregningsgrunnlag.vurder.omp;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp.OmsorgspengerGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(ErDirekteUtbetalingTilBruker.ID)
public class ErDirekteUtbetalingTilBruker extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "K9_9_3.2";
    static final String BESKRIVELSE = "Er det direkte utebetaling til bruker";

	public ErDirekteUtbetalingTilBruker() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
		OmsorgspengerGrunnlag ompGrunnlag = (OmsorgspengerGrunnlag) grunnlag.getBeregningsgrunnlag().getYtelsesSpesifiktGrunnlag();
	    SingleEvaluation resultat = ompGrunnlag.erDirekteUtbetalingPåSkjæringstidspunktet() ? ja() : nei();
        resultat.setEvaluationProperty("erDirekteUtbetalingPåSkjæringstidspunktet", ompGrunnlag.erDirekteUtbetalingPåSkjæringstidspunktet());
        return resultat;
    }


}
