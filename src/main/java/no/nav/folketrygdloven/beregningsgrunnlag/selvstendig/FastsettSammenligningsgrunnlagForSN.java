package no.nav.folketrygdloven.beregningsgrunnlag.selvstendig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SammenligningsGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettSammenligningsgrunnlagForSN.ID)
public class FastsettSammenligningsgrunnlagForSN extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 2.4";
    static final String BESKRIVELSE = "Fastsett sammenligningsgrunnlag og beregn avvik for selvstendig næringsdrivende";

    public FastsettSammenligningsgrunnlagForSN() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        Periodeinntekt oppgittInntekt = grunnlag.getInntektsgrunnlag().getSistePeriodeinntektMedTypeSøknad()
            .orElseThrow(() -> new IllegalStateException("Fant ikke oppgitt månedsinntekt ved varig endret inntekt"));

        SammenligningsGrunnlag sammenligningsGrunnlag = opprettSammenligningsgrunnlag(grunnlag, oppgittInntekt);
        beregnOgFastsettAvvik(grunnlag, sammenligningsGrunnlag);

        if(grunnlag.skalSplitteSammenligningsgrunnlagToggle()){
            Beregningsgrunnlag.builder(grunnlag.getBeregningsgrunnlag()).medSammenligningsgrunnlagPrStatus(AktivitetStatus.SN, sammenligningsGrunnlag).build();
        } else {
            Beregningsgrunnlag.builder(grunnlag.getBeregningsgrunnlag()).medSammenligningsgrunnlag(sammenligningsGrunnlag).build();
        }

        Map<String, Object> resultater = gjørRegelsporing(grunnlag, sammenligningsGrunnlag, oppgittInntekt);
        return beregnet(resultater);
    }

    private void beregnOgFastsettAvvik(BeregningsgrunnlagPeriode grunnlag, SammenligningsGrunnlag sammenligningsGrunnlag) {
        BigDecimal pgiSnitt = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN).getGjennomsnittligPGI();
        BigDecimal sammenligning = sammenligningsGrunnlag.getRapportertPrÅr();
        BigDecimal diff = pgiSnitt.subtract(sammenligning).abs();

        BigDecimal avvikProsent = pgiSnitt.compareTo(BigDecimal.ZERO) != 0
            ? diff.divide(pgiSnitt, 10, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
            : BigDecimal.valueOf(100);

        sammenligningsGrunnlag.setAvvikProsent(avvikProsent);
    }

    private SammenligningsGrunnlag opprettSammenligningsgrunnlag(BeregningsgrunnlagPeriode grunnlag, Periodeinntekt oppgittInntekt) {
        BeregningsgrunnlagPrStatus bgAAP = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.AAP);
        BeregningsgrunnlagPrStatus bgDP = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP);
        BeregningsgrunnlagPrStatus bgATFL = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);

        BigDecimal bruttoAAP = bgAAP != null ? bgAAP.getBeregnetPrÅr() : BigDecimal.ZERO;
        BigDecimal bruttoDP = bgDP != null ? bgDP.getBeregnetPrÅr() : BigDecimal.ZERO;
        BigDecimal bruttoATFL = bgATFL != null ? bgATFL.getBruttoInkludertNaturalytelsePrÅr() : BigDecimal.ZERO;
        BigDecimal antallPerioderPrÅr = oppgittInntekt.getInntektPeriodeType().getAntallPrÅr();
        BigDecimal oppgittÅrsInntekt = oppgittInntekt.getInntekt().multiply(antallPerioderPrÅr);

        BigDecimal sammenligningInntekt = oppgittÅrsInntekt.add(bruttoATFL).add(bruttoAAP).add(bruttoDP);
        Periode sammenligningsperiode = Periode.of(oppgittInntekt.getFom(), oppgittInntekt.getFom().plusMonths(1).minusDays(1));

        return SammenligningsGrunnlag.builder()
            .medSammenligningsperiode(sammenligningsperiode)
            .medRapportertPrÅr(sammenligningInntekt)
            .build();
    }

    private Map<String, Object> gjørRegelsporing(BeregningsgrunnlagPeriode grunnlag, SammenligningsGrunnlag sammenligningsGrunnlag, Periodeinntekt oppgittInntekt) {
        Map<String, Object> resultater = new LinkedHashMap<>();
        BeregningsgrunnlagPrStatus bgAAP = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.AAP);
        BeregningsgrunnlagPrStatus bgDP = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP);
        BeregningsgrunnlagPrStatus bgATFL = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        BeregningsgrunnlagPrStatus bgSN = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);

        String bruttoString = "brutto";
        if (bgATFL != null) {
            String status = "ATFL";
            resultater.put(bruttoString + status, bgATFL.getBeregnetPrÅr());
        }
        if (bgAAP != null) {
            String status = "AAP";
            resultater.put(bruttoString + status, bgAAP.getBeregnetPrÅr());
        }
        if (bgDP != null) {
            String status = "DP";
            resultater.put(bruttoString + status, bgDP.getBeregnetPrÅr());
        }
        resultater.put("gjennomsnittligPGI", bgSN.getGjennomsnittligPGI());
        resultater.put("inntektEtterVarigEndringPrÅr", oppgittInntekt.getInntekt());
        resultater.put("sammenligningsperiode", sammenligningsGrunnlag.getSammenligningsperiode());
        resultater.put("sammenligningsgrunnlagRapportertPrÅr", sammenligningsGrunnlag.getRapportertPrÅr());
        resultater.put("avvikProsent", sammenligningsGrunnlag.getAvvikProsent());

        return resultater;
    }
}
