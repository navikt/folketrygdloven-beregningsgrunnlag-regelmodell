package no.nav.folketrygdloven.beregningsgrunnlag.vurder;

import static no.nav.folketrygdloven.beregningsgrunnlag.vurder.FinnGrunnbeløpForVilkårsvurdering.finnGrunnbeløpForVilkårsvurdering;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkBeregningsgrunnlagMindreEnn.ID)
public class SjekkBeregningsgrunnlagMindreEnn extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_VK_32.1";
    static final String BESKRIVELSE = "Er beregningsgrunnlag mindre enn en 0,5G?";

	public SjekkBeregningsgrunnlagMindreEnn() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {

        BigDecimal grunnbeløpForVilkårsvurdering = finnGrunnbeløpForVilkårsvurdering(grunnlag);

        BigDecimal minstekrav = grunnbeløpForVilkårsvurdering.multiply(grunnlag.getAntallGMinstekravVilkår());

        SingleEvaluation resultat = (grunnlag.getBruttoPrÅr().compareTo(minstekrav) < 0) ? ja() : nei();
        resultat.setEvaluationProperty("grunnbeløpForVilkårsvurdering", grunnbeløpForVilkårsvurdering);
        resultat.setEvaluationProperty("halvtGrunnbeløp", minstekrav);
        resultat.setEvaluationProperty("faktiskGrunnbeløp", grunnlag.getGrunnbeløp());
        resultat.setEvaluationProperty("bruttoPrÅr", grunnlag.getBruttoPrÅr());
        return resultat;
    }


}
