package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;

public abstract class YtelsesSpesifiktGrunnlag {

    protected String ytelseType;
    @JsonBackReference
    protected Beregningsgrunnlag beregningsgrunnlag;

    public YtelsesSpesifiktGrunnlag(String ytelseType) {
        this.ytelseType = ytelseType;
    }

    public String getYtelseType() {
        return ytelseType;
    }

    public void setBeregningsgrunnlag(Beregningsgrunnlag beregningsgrunnlag) {
        this.beregningsgrunnlag = beregningsgrunnlag;
    }

    /**
     * I tilfelle der det er omsorgspenger og det er kun mottatt inntektsmelding for enkelt arbeidsforhold ho en arbeidsgiver
     * skal resterende arbeidsforhold fordele restbeløpet fra a-ordningen mellom seg (https://jira.adeo.no/browse/TSF-1153).
     *
     * I alle andre caser gis det fulle snittbeløpet til dette arbeidsforholdet.
     *
     * @param beregnetPrÅr Snitt de siste 3 månedene fra a-ordningen
     * @param arbeidsforhold Arbeidsforhold
     * @param periode Periode
     * @return Andel av snitt fra a-ordningen
     */
    public abstract BigDecimal finnAndelAvBeregnet(BigDecimal beregnetPrÅr, BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, BeregningsgrunnlagPeriode periode);
}
