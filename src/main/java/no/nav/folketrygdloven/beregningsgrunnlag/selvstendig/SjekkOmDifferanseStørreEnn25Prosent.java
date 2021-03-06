package no.nav.folketrygdloven.beregningsgrunnlag.selvstendig;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SammenligningsGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.RuleReasonRef;
import no.nav.fpsak.nare.evaluation.RuleReasonRefImpl;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmDifferanseStørreEnn25Prosent.ID)
public class SjekkOmDifferanseStørreEnn25Prosent extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 2.5";
    static final String BESKRIVELSE = "Er avvik større enn 25% ?";

    public static final RuleReasonRef VARIG_ENDRING_OG_AVVIK_STØRRE_ENN_25_PROSENT = new RuleReasonRefImpl("5039", "Beregningsgrunnlag fastsettes skjønnsmessig");

    public SjekkOmDifferanseStørreEnn25Prosent() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        final SammenligningsGrunnlag sg;
        if(grunnlag.skalSplitteSammenligningsgrunnlagToggle()){
            sg = grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.SN);
        } else {
            sg = grunnlag.getSammenligningsGrunnlag();
        }

        if (sg == null) {
            throw new IllegalStateException("Sammenligningsgrunnlag mangler");
        }
        return (sg.getAvvikProsent().compareTo(grunnlag.getAvviksgrenseProsent()) > 0 ? ja() : nei());
    }
}
