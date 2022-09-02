package no.nav.folketrygdloven.beregningsgrunnlag.selvstendig;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(BeregnBruttoBeregningsgrunnlagSN.ID)
public class BeregnBruttoBeregningsgrunnlagSN extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 2.8";
    static final String BESKRIVELSE = "Beregn brutto beregningsgrunnlag selvstendig næringsdrivende";

    public BeregnBruttoBeregningsgrunnlagSN() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus bgAAP = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.AAP);
        var bgDP = grunnlag.getBeregningsgrunnlagFraDagpenger();
        boolean harBeregnetAAPEllerDP = (bgAAP != null && bgAAP.getBeregnetPrÅr() == null) ||
			    (bgDP.isPresent() && bgDP.get().getBeregnetPrÅr() == null);
	    if (harBeregnetAAPEllerDP) {
            throw new IllegalStateException("Utviklerfeil: Aktivitetstatuser AAP og DP må beregnes før SN");
        }

        BeregningsgrunnlagPrStatus bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        BigDecimal gjennomsnittligPGI = bgps.getGjennomsnittligPGI() == null ? BigDecimal.ZERO : bgps.getGjennomsnittligPGI();

        BigDecimal bruttoAAP = bgAAP != null ? bgAAP.getBeregnetPrÅr() : BigDecimal.ZERO;
        BigDecimal bruttoDP = bgDP.map(BeregningsgrunnlagPrStatus::getBeregnetPrÅr).orElse(BigDecimal.ZERO);
	    BeregningsgrunnlagPrStatus atflAndel = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
	    BigDecimal bruttoBGArbeidstaker = atflAndel == null ? BigDecimal.ZERO : atflAndel.getBruttoPrÅr();

        BigDecimal bruttoSN = gjennomsnittligPGI.subtract(bruttoAAP).subtract(bruttoDP).subtract(bruttoBGArbeidstaker).max(BigDecimal.ZERO);

        BeregningsgrunnlagPrStatus.builder(bgps).medBeregnetPrÅr(bruttoSN).build();

        Map<String, Object> resultater = new HashMap<>();
        resultater.put("gjennomsnittligPGI", gjennomsnittligPGI);
        if (bruttoAAP.compareTo(BigDecimal.ZERO) > 0) {
            resultater.put("bruttoBeregningsgrunnlagAAP", bruttoAAP);
        }
        if (bruttoDP.compareTo(BigDecimal.ZERO) > 0) {
            resultater.put("bruttoBeregningsgrunnlagDP", bruttoDP);
        }
        resultater.put("bruttoBeregningsgrunnlagSN", bruttoSN);

        return beregnet(resultater);
    }

}
