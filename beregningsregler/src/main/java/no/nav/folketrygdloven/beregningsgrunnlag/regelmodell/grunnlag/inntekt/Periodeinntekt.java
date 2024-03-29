package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;

public class Periodeinntekt {
    private Periode periode;
    private BigDecimal inntekt;
    private Inntektskilde inntektskilde;
    private Arbeidsforhold arbeidsgiver;
    private BigDecimal utbetalingsfaktor;
    private InntektPeriodeType inntektPeriodeType;
    private List<NaturalYtelse> naturalYtelser = new ArrayList<>();
    // Kun i bruk for splitting av sammenligningsgrunnlag og besteberegning. Burde besteberegning ha brukt inntektskategori i staden?
    private AktivitetStatus aktivitetStatus;
	private Inntektskategori inntektskategori;

	private RelatertYtelseType ytelse;

    public BigDecimal getInntekt() {
        return inntekt;
    }

    public boolean inneholder(LocalDate dato) {
        return !(dato.isBefore(getFom()) || dato.isAfter(getTom()));
    }

    public boolean erFraår(int år) {
        return år == getFom().getYear();
    }

    public Inntektskilde getInntektskilde() {
        return inntektskilde;
    }

    public Optional<Arbeidsforhold> getArbeidsgiver() {
        return Optional.ofNullable(arbeidsgiver);
    }

	public Periode getPeriode() {
		return periode;
	}

	public LocalDate getFom() {
        return periode.getFom();
    }

    public LocalDate getTom() {
        return periode.getTom();
    }

    public Optional<BigDecimal> getUtbetalingsfaktor() {
        return Optional.ofNullable(utbetalingsfaktor);
    }

	public RelatertYtelseType getYtelse() {
		return ytelse;
	}

	public List<NaturalYtelse> getNaturalYtelser() {
        return Collections.unmodifiableList(naturalYtelser);
    }

    public boolean fraInntektsmelding() {
        return Inntektskilde.INNTEKTSMELDING.equals(inntektskilde);
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

	public Inntektskategori getInntektskategori() {
		return inntektskategori;
	}

	public boolean erInnenforPeriode(Periode periode) {
        return !getFom().isBefore(periode.getFom()) && !getTom().isAfter(periode.getTom());
    }

    public InntektPeriodeType getInntektPeriodeType() {
        return inntektPeriodeType;
    }

    public boolean erArbeidstaker(){
        return AktivitetStatus.AT.equals(this.aktivitetStatus);
    }

    public boolean erFrilans(){
        return AktivitetStatus.FL.equals(this.aktivitetStatus);
    }

    public boolean erSelvstendingNæringsdrivende(){
        return AktivitetStatus.SN.equals(this.aktivitetStatus);
    }

    //Eneste tillate oppretting av en periodeinntekt da feltene skal være effektivt final (uten å være det for builderens skyld)
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Periodeinntekt kladd;

        private Builder() {
            kladd = new Periodeinntekt();
        }

        public Builder medInntektskildeOgPeriodeType(Inntektskilde inntektskilde) {
            if (inntektskilde == null) {
                throw new IllegalArgumentException("Inntektskilde kan ikke være null.");
            }
            kladd.inntektskilde = inntektskilde;
            kladd.inntektPeriodeType = inntektskilde.getInntektPeriodeType();
            return this;
        }

        public Builder medPeriode(Periode periode) {
            kladd.periode = periode;
            return this;
        }

	    public Builder medYtelse(RelatertYtelseType ytelse) {
		    kladd.ytelse = ytelse;
		    return this;
	    }

	    public Builder medMåned(LocalDate dato) {
            kladd.periode = Periode.of(dato.withDayOfMonth(1), dato.withDayOfMonth(1).plusMonths(1).minusDays(1));
            return this;
        }

	    public Builder medMåned(YearMonth måned) {
			kladd.periode = Periode.of(måned.atDay(1), måned.atEndOfMonth());
		    return this;
	    }

        public Builder medArbeidsgiver(Arbeidsforhold arbeidsgiver) {
            kladd.arbeidsgiver = arbeidsgiver;
            return this;
        }

        public Builder medInntekt(BigDecimal inntekt) {
            kladd.inntekt = inntekt;
            return this;
        }

        public Builder medUtbetalingsfaktor(BigDecimal utbetalingsgrad) {
        	if (utbetalingsgrad.compareTo(BigDecimal.valueOf(1)) > 0) {
        		throw new IllegalArgumentException("Utbetalingsgrad må oppgis som et tall mellom 0 og 1. Oppgitt utbetalingsgrad: " + utbetalingsgrad);
	        }
            kladd.utbetalingsfaktor = utbetalingsgrad;
            return this;
        }

        public Builder medNaturalYtelser(List<NaturalYtelse> naturalYtelser) {
            if (naturalYtelser != null) {
                kladd.naturalYtelser.addAll(naturalYtelser);
            }
            return this;
        }

        public Builder medAktivitetStatus(AktivitetStatus aktivitetStatus) {
            kladd.aktivitetStatus = aktivitetStatus;
            return this;
        }

	    public Builder medInntektskategori(Inntektskategori inntektskategori) {
		    kladd.inntektskategori = inntektskategori;
		    return this;
	    }

        public Periodeinntekt build() {
            verifyStateForBuild();
            return kladd;
        }

        private void verifyStateForBuild() {
            Objects.requireNonNull(kladd.inntekt, "Inntekt");
            Objects.requireNonNull(kladd.inntektskilde, "Inntektskilde");
            if(!Inntektskilde.INNTEKTSMELDING.equals(kladd.inntektskilde)) {
                Objects.requireNonNull(kladd.periode, "Periode");
                Objects.requireNonNull(kladd.getFom(), "Fom");
                Objects.requireNonNull(kladd.getTom(), "Tom");
            }
            Objects.requireNonNull(kladd.inntektPeriodeType);

            if (!kladd.getNaturalYtelser().isEmpty() && !kladd.fraInntektsmelding()) {
                throw new IllegalArgumentException("Naturalytelse kan bare angis med kilde Inntektsmelding");
            }
        }

    }
}
