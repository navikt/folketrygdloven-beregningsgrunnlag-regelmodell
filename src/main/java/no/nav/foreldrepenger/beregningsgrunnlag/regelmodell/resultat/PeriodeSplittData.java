package no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.ArbeidsforholdOgInntektsmelding;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.PeriodeÅrsak;

public class PeriodeSplittData {

    private LocalDate fom;
    private LocalDate tom;
    private PeriodeÅrsak periodeÅrsak;
    private BigDecimal refusjonskravPrMåned;
    private ArbeidsforholdOgInntektsmelding inntektsmelding;

    private PeriodeSplittData() {
        //private constructor
    }

    public PeriodeÅrsak getPeriodeÅrsak() {
        return periodeÅrsak;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public void setTom(LocalDate tom) {
        this.tom = tom;
    }

    public BigDecimal getRefusjonskravPrMåned() {
        return refusjonskravPrMåned;
    }

    public ArbeidsforholdOgInntektsmelding getInntektsmelding() {
        return inntektsmelding;
    }

    @Override
    public String toString() {
        return "PeriodeSplittData{" +
            "fom=" + fom +
            ", periodeÅrsak=" + periodeÅrsak +
            ", refusjonskravPrMåned=" + refusjonskravPrMåned +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PeriodeSplittData that = (PeriodeSplittData) o;
        return Objects.equals(fom, that.fom)
            && Objects.equals(tom, that.tom)
            && Objects.equals(periodeÅrsak, that.periodeÅrsak)
            && Objects.equals(refusjonskravPrMåned, that.refusjonskravPrMåned)
            && Objects.equals(inntektsmelding, that.inntektsmelding);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fom, tom, periodeÅrsak, refusjonskravPrMåned, inntektsmelding);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private PeriodeSplittData kladd;

        private Builder() {
            kladd = new PeriodeSplittData();
        }

        public Builder medPeriodeÅrsak(PeriodeÅrsak periodeÅrsak) {
            kladd.periodeÅrsak = periodeÅrsak;
            return this;
        }

        public Builder medFom(LocalDate fom) {
            kladd.fom = fom;
            return this;
        }

        public Builder medRefusjonskravPrMåned(BigDecimal refusjonskravPrMåned) {
            kladd.refusjonskravPrMåned = refusjonskravPrMåned;
            return this;
        }

        public Builder medInntektsmelding(ArbeidsforholdOgInntektsmelding inntektsmelding) {
            kladd.inntektsmelding = inntektsmelding;
            return this;
        }

        public PeriodeSplittData build() {
            Objects.requireNonNull(kladd.periodeÅrsak);
            return kladd;
        }
    }
}
