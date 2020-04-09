package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

import java.util.HashMap;
import java.util.Map;

@RuleDocumentation(BeregnBruttoBeregningsgrunnlagSNFRISINN.ID)
public class BeregnBruttoBeregningsgrunnlagSNFRISINN extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FRISINN 2.8";
    static final String BESKRIVELSE = "Beregn brutto beregningsgrunnlag selvstendig næringsdrivende";

    public BeregnBruttoBeregningsgrunnlagSNFRISINN() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        Periodeinntekt oppgittInntekt = grunnlag.getInntektsgrunnlag().getSistePeriodeinntektMedTypeSøknad().orElse(null);

        if (oppgittInntekt == null || oppgittInntekt.getInntekt() == null) {
            throw new IllegalStateException("Finner ikke oppgitt inntekt fra søker som beregnes som SN. Ugyldig tilstand for FRISINN.");
        }

        BeregningsgrunnlagPrStatus bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        BeregningsgrunnlagPrStatus.builder(bgps).medBeregnetPrÅr(oppgittInntekt.getInntekt()).build();
        Map<String, Object> resultater = new HashMap<>();
        resultater.put("oppgittInntekt", oppgittInntekt.getInntekt());
        return beregnet(resultater);
    }

}
