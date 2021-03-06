package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmInntektsmeldingForeligger.ID)
class SjekkOmInntektsmeldingForeligger extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 15.1";
    static final String BESKRIVELSE = "Foreligger inntektsmelding?";
    private BeregningsgrunnlagPrArbeidsforhold arbeidsforhold;

    SjekkOmInntektsmeldingForeligger(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        super(ID, BESKRIVELSE);
        this.arbeidsforhold = arbeidsforhold;
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        if (arbeidsforhold.erFrilanser()) {
            return nei();
        }
        return grunnlag.getInntektsgrunnlag().finnesInntektsdata(Inntektskilde.INNTEKTSMELDING, arbeidsforhold) ? ja() : nei();
    }
}
