package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;

public class BeregningsgrunnlagPrArbeidsforhold {
    private BigDecimal naturalytelseBortfaltPrÅr;
    private BigDecimal naturalytelseTilkommetPrÅr;
    private BigDecimal beregnetPrÅr;
    private BigDecimal overstyrtPrÅr;
	private BigDecimal besteberegningPrÅr;
	private BigDecimal fordeltPrÅr;
    private BigDecimal avkortetPrÅr;
    private BigDecimal redusertPrÅr;
    private Periode beregningsperiode;
    private Arbeidsforhold arbeidsforhold;

    // Refusjonskravet som skal gjelde for arbeidsforholdet
    private BigDecimal gjeldendeRefusjonPrÅr;

    // Refusjonskravet etter omfordeling, må ha eget felt for å kunne mappes ut.
    // TODO TFP-3865 skill fordeling fra resten av beregningsregelmodellen
	private BigDecimal fordeltRefusjonPrÅr;

    private BigDecimal maksimalRefusjonPrÅr;
    private BigDecimal avkortetRefusjonPrÅr;
    private BigDecimal redusertRefusjonPrÅr;
    private BigDecimal avkortetBrukersAndelPrÅr;
    private BigDecimal redusertBrukersAndelPrÅr;
    private Long dagsatsBruker;
    private Long dagsatsArbeidsgiver;
    private Boolean tidsbegrensetArbeidsforhold;
    private Boolean fastsattAvSaksbehandler;
    private Boolean lagtTilAvSaksbehandler;
    private Long andelNr;
    private Inntektskategori inntektskategori;
    private List<Periode> arbeidsgiverperioder = new ArrayList<>(); //Brukes i beregning for sykepenger
    private Boolean erSøktYtelseFor;
    private BigDecimal utbetalingsprosent = BigDecimal.valueOf(100);
    private BigDecimal andelsmessigFørGraderingPrAar;

    public BeregningsgrunnlagPrArbeidsforhold() {
    }

    public String getArbeidsgiverId() {
        return arbeidsforhold.getArbeidsgiverId();
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public boolean erFrilanser() {
        return arbeidsforhold.erFrilanser();
    }

    public Optional<BigDecimal> getNaturalytelseBortfaltPrÅr() {
        return Optional.ofNullable(naturalytelseBortfaltPrÅr);
    }

    public Optional<BigDecimal> getGradertNaturalytelseBortfaltPrÅr() {
        return Optional.ofNullable(finnGradert(naturalytelseBortfaltPrÅr));
    }

    public Optional<BigDecimal> getNaturalytelseTilkommetPrÅr() {
        return Optional.ofNullable(naturalytelseTilkommetPrÅr);
    }

    public Optional<BigDecimal> getGradertNaturalytelseTilkommetPrÅr() {
        return Optional.ofNullable(finnGradert(naturalytelseTilkommetPrÅr));
    }

    public BigDecimal getBeregnetPrÅr() {
        return beregnetPrÅr;
    }

    public String getBeskrivelse() {
        return (erFrilanser() ? "FL:" : "AT:") + getArbeidsgiverId();
    }

    public BigDecimal getOverstyrtPrÅr() {
        return overstyrtPrÅr;
    }

	/** Finner brutto pr år for arbeidsforhold
	 *
	 * Prioriteringsrekkefølge for felter framgår av rekkefølgen de settes i ft-kalkulus.
	 *
	 * @return BruttoPrÅr
	 */
	// TODO (TFP-3955) Fjern avhengighet til prosess i kalkulus. Krever egen regelmodell for kvar regelsteg (FORESLÅ, FASTSETT)
    public Optional<BigDecimal> getBruttoPrÅr() {
    	if (fordeltPrÅr != null) {
    		return Optional.of(fordeltPrÅr);
	    }
    	if (besteberegningPrÅr != null) {
    		return Optional.of(besteberegningPrÅr);
	    }
    	if (overstyrtPrÅr != null) {
    		return Optional.of(overstyrtPrÅr);
	    }
    	if (beregnetPrÅr != null) {
    		return Optional.of(beregnetPrÅr);
	    }
        return Optional.empty();
    }

    public BigDecimal getGradertBruttoPrÅr() {
        return finnGradert(getBruttoPrÅr().orElse(null));
    }

    public BigDecimal getFordeltPrÅr() {
        return fordeltPrÅr;
    }

    public BigDecimal getGradertFordeltPrÅr() {
        return finnGradert(fordeltPrÅr);
    }

	public BigDecimal getFordeltRefusjonPrÅr() {
		return fordeltRefusjonPrÅr;
	}

	public Optional<BigDecimal> getBruttoInkludertNaturalytelsePrÅr() {
        if (getBruttoPrÅr().isEmpty()) {
            return Optional.empty();
        }
        BigDecimal bortfaltNaturalytelse = naturalytelseBortfaltPrÅr != null ? naturalytelseBortfaltPrÅr : BigDecimal.ZERO;
        BigDecimal tilkommetNaturalytelse = naturalytelseTilkommetPrÅr != null ? naturalytelseTilkommetPrÅr : BigDecimal.ZERO;
        return Optional.of(getBruttoPrÅr().get().add(bortfaltNaturalytelse).subtract(tilkommetNaturalytelse));
    }

    public BigDecimal getAvkortetPrÅr() {
        return avkortetPrÅr;
    }

    public BigDecimal getRedusertPrÅr() {
        return redusertPrÅr;
    }

    public Periode getBeregningsperiode() {
        return beregningsperiode;
    }

    public Arbeidsforhold getArbeidsforhold() {
        return arbeidsforhold;
    }

	public Optional<BigDecimal> getGjeldendeRefusjonPrÅr() {
		return Optional.ofNullable(gjeldendeRefusjonPrÅr);
	}

    public Optional<BigDecimal> getGradertRefusjonskravPrÅr() {
        return Optional.ofNullable(finnGradert(gjeldendeRefusjonPrÅr));
    }

    public Optional<BigDecimal> getGradertBruttoInkludertNaturalytelsePrÅr() {
        Optional<BigDecimal> brutto = getBruttoInkludertNaturalytelsePrÅr();
        return brutto.map(this::finnGradert);
    }

    public BigDecimal getMaksimalRefusjonPrÅr() {
        return maksimalRefusjonPrÅr;
    }

    public Long getDagsats() {
        if (dagsatsBruker == null) {
            return dagsatsArbeidsgiver;
        }
        if (dagsatsArbeidsgiver == null) {
            return dagsatsBruker;
        }
        return dagsatsBruker + dagsatsArbeidsgiver;
    }

    public BigDecimal getAvkortetRefusjonPrÅr() {
        return avkortetRefusjonPrÅr;
    }

    public BigDecimal getRedusertRefusjonPrÅr() {
        return redusertRefusjonPrÅr;
    }

    public BigDecimal getAvkortetBrukersAndelPrÅr() {
        return avkortetBrukersAndelPrÅr;
    }

    public BigDecimal getRedusertBrukersAndelPrÅr() {
        return redusertBrukersAndelPrÅr;
    }

    public Long getDagsatsBruker() {
        return dagsatsBruker;
    }

    public Long getDagsatsArbeidsgiver() {
        return dagsatsArbeidsgiver;
    }

    public Boolean getTidsbegrensetArbeidsforhold() {
        return tidsbegrensetArbeidsforhold;
    }

    public Boolean getFastsattAvSaksbehandler() {
        return fastsattAvSaksbehandler != null && fastsattAvSaksbehandler;
    }

    public Boolean getLagtTilAvSaksbehandler() {
        return lagtTilAvSaksbehandler;
    }

    public Long getAndelNr() {
        return andelNr;
    }

    public List<Periode> getArbeidsgiverperioder() {
        return Collections.unmodifiableList(arbeidsgiverperioder);
    }

    public BigDecimal getUtbetalingsprosent() {
        return utbetalingsprosent;
    }

    public boolean getErSøktYtelseFor() {
        return erSøktYtelseFor != null ? erSøktYtelseFor : utbetalingsprosent.compareTo(BigDecimal.ZERO) > 0;
    }

    public void setErSøktYtelseFor(boolean erSøktYtelseFor) {
        this.erSøktYtelseFor = erSøktYtelseFor;
    }

    public BigDecimal getAndelsmessigFørGraderingPrAar() {
        return andelsmessigFørGraderingPrAar;
    }

    @Override
    public String toString() {
        return getBeskrivelse();
    }

    private BigDecimal finnGradert(BigDecimal verdi) {
        return verdi == null ? null : verdi.multiply(utbetalingsprosent.scaleByPowerOfTen(-2));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(BeregningsgrunnlagPrArbeidsforhold af) {
        return new Builder(af);
    }
    public static class Builder {

        private BeregningsgrunnlagPrArbeidsforhold mal;
        private boolean erNytt;

        public Builder() {
            mal = new BeregningsgrunnlagPrArbeidsforhold();
        }

        public Builder(BeregningsgrunnlagPrArbeidsforhold af) {
            mal = af;
        }

        public Builder medArbeidsforhold(Arbeidsforhold arbeidsforhold) {
            mal.arbeidsforhold = arbeidsforhold;
            return this;
        }


        public Builder erNytt(boolean erNytt) {
            this.erNytt = erNytt;
            return this;
        }

        public Builder medAndelsmessigFørGraderingPrAar(BigDecimal andelsmessigFørGraderingPrAar) {
            mal.andelsmessigFørGraderingPrAar = andelsmessigFørGraderingPrAar;
            return this;
        }

        public Builder medBeregnetPrÅr(BigDecimal beregnetPrÅr) {
            mal.beregnetPrÅr = beregnetPrÅr;
            return this;
        }

        public Builder medOverstyrtPrÅr(BigDecimal overstyrtPrÅr) {
            mal.overstyrtPrÅr = overstyrtPrÅr;
            return this;
        }

        public Builder medFordeltPrÅr(BigDecimal fordeltPrÅr) {
            mal.fordeltPrÅr = fordeltPrÅr;
            return this;
        }

        public Builder medAvkortetPrÅr(BigDecimal avkortetPrÅr) {
            mal.avkortetPrÅr = avkortetPrÅr;
            return this;
        }

        public Builder medRedusertPrÅr(BigDecimal redusertPrÅr) {
            mal.redusertPrÅr = redusertPrÅr;
            return this;
        }

        public Builder medNaturalytelseBortfaltPrÅr(BigDecimal naturalytelseBortfaltPrÅr) {
            mal.naturalytelseBortfaltPrÅr = naturalytelseBortfaltPrÅr;
            return this;
        }

        public Builder medNaturalytelseTilkommetPrÅr(BigDecimal naturalytelseTilkommetPrÅr) {
            mal.naturalytelseTilkommetPrÅr = naturalytelseTilkommetPrÅr;
            return this;
        }

        public Builder medBeregningsperiode(Periode beregningsperiode) {
            mal.beregningsperiode = beregningsperiode;
            return this;
        }

        public Builder medGjeldendeRefusjonPrÅr(BigDecimal gjeldendeRefusjonPrÅr) {
            mal.gjeldendeRefusjonPrÅr = gjeldendeRefusjonPrÅr;
            return this;
        }

	    public Builder medFordeltRefusjonPrÅr(BigDecimal fordeltRefusjonPrÅr) {
		    mal.fordeltRefusjonPrÅr = fordeltRefusjonPrÅr;
		    return this;
	    }

	    public Builder medMaksimalRefusjonPrÅr(BigDecimal maksimalRefusjonPrÅr) {
            mal.maksimalRefusjonPrÅr = maksimalRefusjonPrÅr;
            return this;
        }

        public Builder medAvkortetRefusjonPrÅr(BigDecimal avkortetRefusjonPrÅr) {
            mal.avkortetRefusjonPrÅr = avkortetRefusjonPrÅr;
            return this;
        }

	    public Builder medBesteberegningPrÅr(BigDecimal besteberegningPrÅr) {
		    mal.besteberegningPrÅr = besteberegningPrÅr;
		    return this;
	    }

	    public Builder medRedusertRefusjonPrÅr(BigDecimal redusertRefusjonPrÅr, BigDecimal ytelsesdagerPrÅr) {
            mal.redusertRefusjonPrÅr = redusertRefusjonPrÅr;
            mal.dagsatsArbeidsgiver = redusertRefusjonPrÅr == null || ytelsesdagerPrÅr == null ? null : redusertRefusjonPrÅr.divide(ytelsesdagerPrÅr, 0, RoundingMode.HALF_UP).longValue();
            return this;
        }

        public Builder medAvkortetBrukersAndelPrÅr(BigDecimal avkortetBrukersAndelPrÅr) {
            mal.avkortetBrukersAndelPrÅr = avkortetBrukersAndelPrÅr;
            return this;
        }

        public Builder medRedusertBrukersAndelPrÅr(BigDecimal redusertBrukersAndelPrÅr, BigDecimal ytelsesdagerPrÅr) {
            mal.redusertBrukersAndelPrÅr = redusertBrukersAndelPrÅr;
            mal.dagsatsBruker = redusertBrukersAndelPrÅr == null || ytelsesdagerPrÅr == null ? null : redusertBrukersAndelPrÅr.divide(ytelsesdagerPrÅr, 0, RoundingMode.HALF_UP).longValue();
            return this;
        }

        public Builder medErTidsbegrensetArbeidsforhold(Boolean tidsbegrensetArbeidsforhold) {
            mal.tidsbegrensetArbeidsforhold = tidsbegrensetArbeidsforhold;
            return this;
        }

        public Builder medAndelNr(long andelNr) {
            mal.andelNr = andelNr;
            return this;
        }

        public Builder medInntektskategori(Inntektskategori inntektskategori) {
            mal.inntektskategori = inntektskategori;
            return this;
        }

        public Builder medFastsattAvSaksbehandler(Boolean fastsattAvSaksbehandler) {
            mal.fastsattAvSaksbehandler = fastsattAvSaksbehandler;
            return this;
        }

        public Builder medLagtTilAvSaksbehandler(Boolean lagtTilAvSaksbehandler) {
            mal.lagtTilAvSaksbehandler = lagtTilAvSaksbehandler;
            return this;
        }

        //Brukes bare i sykepenger og enhetstest
        public Builder medArbeidsgiverperioder(List<Periode> arbeidsgiverperioder) {
            mal.arbeidsgiverperioder.addAll(arbeidsgiverperioder);
            return this;
        }

        public Builder medUtbetalingsprosent(BigDecimal utbetalingsprosent) {
            mal.utbetalingsprosent = utbetalingsprosent;
            return this;
        }

        public BeregningsgrunnlagPrArbeidsforhold build() {
            verifyStateForBuild();
            return mal;
        }

        private void verifyStateForBuild() {
            Objects.requireNonNull(mal.arbeidsforhold, "arbeidsforhold");
            if (!erNytt) {
                Objects.requireNonNull(mal.andelNr, "andelNr");
            }
        }
    }
}
