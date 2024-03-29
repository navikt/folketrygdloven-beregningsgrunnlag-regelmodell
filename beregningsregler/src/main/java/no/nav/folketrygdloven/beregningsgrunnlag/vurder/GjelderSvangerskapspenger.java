package no.nav.folketrygdloven.beregningsgrunnlag.vurder;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.svp.SvangerskapspengerGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(GjelderSvangerskapspenger.ID)
class GjelderSvangerskapspenger extends LeafSpecification<BeregningsgrunnlagPeriode> {

	static final String ID = "FP_BR_29";

	GjelderSvangerskapspenger() {
		super(ID, "Gjelder det svangerskapspenger?");
	}

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
		return grunnlag.getBeregningsgrunnlag().getYtelsesSpesifiktGrunnlag() instanceof SvangerskapspengerGrunnlag ? ja() : nei();
	}


}
