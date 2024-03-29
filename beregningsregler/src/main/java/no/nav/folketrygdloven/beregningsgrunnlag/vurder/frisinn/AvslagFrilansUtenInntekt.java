package no.nav.folketrygdloven.beregningsgrunnlag.vurder.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(AvslagFrilansUtenInntekt.ID)
public class AvslagFrilansUtenInntekt extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FRISINN 3.5";
    static final String BESKRIVELSE = "Opprett regelmerknad om avslag grunnet frilans uten inntekt";

    public AvslagFrilansUtenInntekt() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
	    return nei(new BeregningUtfallMerknad(BeregningUtfallÅrsak.FRISINN_FRILANS_UTEN_INNTEKT));
    }
}
