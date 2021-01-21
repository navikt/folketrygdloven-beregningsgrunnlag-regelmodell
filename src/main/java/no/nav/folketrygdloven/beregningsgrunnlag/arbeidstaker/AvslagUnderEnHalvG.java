package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.RuleReasonRefImpl;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(AvslagUnderEnHalvG.ID)
public class AvslagUnderEnHalvG extends LeafSpecification<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_VK_32.2";
    public static final String BESKRIVELSE = "Opprett regelmerknad om avslag under 0.5G";
    public static final String AVSLAGSÅRSAK = "1041";

    public AvslagUnderEnHalvG() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        return nei(new RuleReasonRefImpl(AVSLAGSÅRSAK, BESKRIVELSE));
    }
}
