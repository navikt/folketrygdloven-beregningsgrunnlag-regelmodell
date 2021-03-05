package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;

public class BruttoBeregningsgrunnlag {
    private AktivitetStatusV2 aktivitetStatus;
    private Arbeidsforhold arbeidsforhold;
    private BigDecimal bruttoPrÅr;
    private BigDecimal refusjonPrÅr;

    private BruttoBeregningsgrunnlag() {
        // skjul default
    }

    public BigDecimal getBruttoPrÅr() {
        return bruttoPrÅr == null ? BigDecimal.ZERO : bruttoPrÅr;
    }

	public BigDecimal getRefusjonPrÅr() {
		return refusjonPrÅr == null ? BigDecimal.ZERO : refusjonPrÅr;
	}

	public AktivitetStatusV2 getAktivitetStatus() {
        return aktivitetStatus;
    }

    public Optional<Arbeidsforhold> getArbeidsforhold() {
        return Optional.ofNullable(arbeidsforhold);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final BruttoBeregningsgrunnlag kladd;

        private Builder() {
            kladd = new BruttoBeregningsgrunnlag();
        }

        public Builder medAktivitetStatus(AktivitetStatusV2 aktivitetStatus) {
            kladd.aktivitetStatus = aktivitetStatus;
            return this;
        }

        public Builder medArbeidsforhold(Arbeidsforhold arbeidsforhold) {
            kladd.arbeidsforhold = arbeidsforhold;
            return this;
        }

        public Builder medBruttoPrÅr(BigDecimal bruttoBg) {
            kladd.bruttoPrÅr = bruttoBg;
            return this;
        }

	    public Builder medRefusjonPrÅr(BigDecimal refusjonPrÅr) {
		    kladd.refusjonPrÅr = refusjonPrÅr;
		    return this;
	    }

        public BruttoBeregningsgrunnlag build() {
            Objects.requireNonNull(kladd.getAktivitetStatus(), "aktivitetStatus");
            return kladd;
        }
    }
}
