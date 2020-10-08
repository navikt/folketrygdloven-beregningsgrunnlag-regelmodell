package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.kombinasjon;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(BeregnSelvstendigAndelForKombinasjon.ID)
public class BeregnSelvstendigAndelForKombinasjon extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 2.10";
    static final String BESKRIVELSE = "Beregn selvstendig næringsdrivendeandel for kombinasjonen av statusene ATFL og SN";

    public BeregnSelvstendigAndelForKombinasjon() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus bgAAP = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.AAP);
        BeregningsgrunnlagPrStatus bgDP = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP);
        if ((bgAAP != null && bgAAP.getBeregnetPrÅr() == null) || (bgDP != null && bgDP.getBeregnetPrÅr() == null)) {
            throw new IllegalStateException("Utviklerfeil: Aktivitetstatuser AAP og DP må beregnes før SN");
        }
        BeregningsgrunnlagPrStatus bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        BigDecimal gjsnittPGI = bgps.getGjennomsnittligPGI();
        BigDecimal bruttoBGArbeidstaker = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getBruttoPrÅr();

        BigDecimal bruttoAAP = bgAAP != null ? bgAAP.getBeregnetPrÅr() : BigDecimal.ZERO;
        BigDecimal bruttoDP = bgDP != null ? bgDP.getBeregnetPrÅr() : BigDecimal.ZERO;

        BigDecimal bruttoBGSN = BigDecimal.ZERO.max(gjsnittPGI.subtract(bruttoBGArbeidstaker).subtract(bruttoAAP).subtract(bruttoDP));

        BeregningsgrunnlagPrStatus.builder(bgps).medBeregnetPrÅr(bruttoBGSN).build();
        Map<String, Object> resultater = new LinkedHashMap<>();
        if (bruttoAAP.compareTo(BigDecimal.ZERO) > 0) {
            resultater.put("bruttoAAP", bruttoAAP);
        }
        if (bruttoDP.compareTo(BigDecimal.ZERO) > 0) {
            resultater.put("bruttoDP", bruttoDP);
        }
        resultater.put("selvstendigNæringsDrivendeAndel", bruttoBGSN);
        return beregnet(resultater);
    }
}
