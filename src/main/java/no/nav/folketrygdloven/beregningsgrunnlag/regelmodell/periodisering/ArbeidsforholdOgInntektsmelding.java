package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Gradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.NaturalYtelse;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetAndel;
import no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil;

public class ArbeidsforholdOgInntektsmelding implements AndelEndring {
    private Arbeidsforhold arbeidsforhold;
    private List<Refusjonskrav> refusjoner = Collections.emptyList();
    private List<Refusjonskrav> gyldigeRefusjonskrav = Collections.emptyList();
    private List<Gradering> graderinger = Collections.emptyList();
    private List<NaturalYtelse> naturalYtelser = Collections.emptyList();
    private Periode ansettelsesperiode;
    private LocalDate startdatoPermisjon;
    private Long andelsnr;
    private LocalDate innsendingsdatoFørsteInntektsmeldingMedRefusjon;
    private LocalDate overstyrtRefusjonsFrist;
    private RefusjonskravFrist refusjonskravFrist;

    @Override
    public Arbeidsforhold getArbeidsforhold() {
        return arbeidsforhold;
    }

	@Override
	public boolean filterForEksisterendeAktiviteter() {
		return andelsnr != null;
	}

	@Override
	public boolean filterForNyeAktiviteter(LocalDate skjæringstidspunkt, LocalDate periodeFom) {
		return !slutterFørSkjæringstidspunkt(skjæringstidspunkt)
		&& harRefusjonIPeriode(periodeFom);
	}


	private boolean harRefusjonIPeriode(LocalDate periodeFom) {
		return getGyldigeRefusjonskrav().stream()
				.filter(refusjonskrav -> refusjonskrav.getPeriode().inneholder(periodeFom))
				.anyMatch(refusjonskrav -> refusjonskrav.getMånedsbeløp().compareTo(BigDecimal.ZERO) > 0);
	}


	@Override
	public EksisterendeAndel mapForEksisterendeAktiviteter(LocalDate fom) {
		Optional<BigDecimal> refusjonskravPrÅr = getGyldigeRefusjonskrav().stream()
				.filter(refusjon -> refusjon.getPeriode().inneholder(fom))
				.findFirst()
				.map(refusjonskrav -> refusjonskrav.getMånedsbeløp().multiply(BigDecimal.valueOf(12)));
		Optional<BigDecimal> naturalytelseBortfaltPrÅr = getNaturalYtelser().stream()
				.filter(naturalYtelse -> naturalYtelse.getFom().isEqual(DateUtil.TIDENES_BEGYNNELSE))
				.filter(naturalYtelse -> naturalYtelse.getTom().isBefore(fom))
				.map(NaturalYtelse::getBeløp)
				.reduce(BigDecimal::add);
		Optional<BigDecimal> naturalytelseTilkommer = getNaturalYtelser().stream()
				.filter(naturalYtelse -> naturalYtelse.getTom().isEqual(DateUtil.TIDENES_ENDE))
				.filter(naturalYtelse -> naturalYtelse.getFom().isBefore(fom))
				.map(NaturalYtelse::getBeløp)
				.reduce(BigDecimal::add);
		return EksisterendeAndel.builder()
				.medAndelNr(getAndelsnr())
				.medRefusjonskravPrÅr(refusjonskravPrÅr.orElse(null))
				.medNaturalytelseTilkommetPrÅr(naturalytelseTilkommer.orElse(null))
				.medNaturalytelseBortfaltPrÅr(naturalytelseBortfaltPrÅr.orElse(null))
				.medArbeidsforhold(getArbeidsforhold())
				.medAnvendtRefusjonskravfristHjemmel(getRefusjonskravFrist().map(RefusjonskravFrist::getAnvendtHjemmel).orElse(null))
				.build();
	}

	@Override
	public SplittetAndel mapForNyeAktiviteter(LocalDate periodeFom) {
		return mapSplittetAndel(periodeFom);
	}

	private SplittetAndel mapSplittetAndel(LocalDate periodeFom) {
		BigDecimal refusjonPrÅr = getGyldigeRefusjonskrav().stream()
				.filter(refusjonskrav -> refusjonskrav.getPeriode().inneholder(periodeFom))
				.map(refusjonskrav -> refusjonskrav.getMånedsbeløp().multiply(BigDecimal.valueOf(12)))
				.findFirst().orElse(BigDecimal.ZERO);
		SplittetAndel.Builder builder = SplittetAndel.builder()
				.medAktivitetstatus(getAktivitetStatus())
				.medArbeidsforhold(getArbeidsforhold())
				.medRefusjonskravPrÅr(refusjonPrÅr)
				.medAnvendtRefusjonskravfristHjemmel(getRefusjonskravFrist().map(RefusjonskravFrist::getAnvendtHjemmel).orElse(null));
		return builder.build();
	}

	public List<Refusjonskrav> getRefusjoner() {
        return refusjoner;
    }

    @Override
    public List<Refusjonskrav> getGyldigeRefusjonskrav() {
        return gyldigeRefusjonskrav;
    }

    public void setGyldigeRefusjonskrav(List<Refusjonskrav> gyldigeRefusjonskrav) {
        this.gyldigeRefusjonskrav = gyldigeRefusjonskrav;
    }

    @Override
    public AktivitetStatusV2 getAktivitetStatus() {
        return AktivitetStatusV2.AT;
    }

    @Override
    public List<Gradering> getGraderinger() {
        return graderinger;
    }

    public List<NaturalYtelse> getNaturalYtelser() {
        return naturalYtelser;
    }

    public Periode getAnsettelsesperiode() {
        return ansettelsesperiode;
    }

    public LocalDate getStartdatoPermisjon() {
        return startdatoPermisjon;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public LocalDate getInnsendingsdatoFørsteInntektsmeldingMedRefusjon() {
        return innsendingsdatoFørsteInntektsmeldingMedRefusjon;
    }

    public Optional<LocalDate> getOverstyrtRefusjonsFrist() {
        return Optional.ofNullable(overstyrtRefusjonsFrist);
    }

    public Optional<RefusjonskravFrist> getRefusjonskravFrist() {
        return Optional.ofNullable(refusjonskravFrist);
    }

    @Override
    public boolean erNyAktivitet(PeriodeModell input, LocalDate periodeFom) { return andelsnr == null; }

    public boolean slutterFørSkjæringstidspunkt(LocalDate skjæringstidspunkt) {
        return ansettelsesperiode.getTom().isBefore(skjæringstidspunkt.minusDays(1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArbeidsforholdOgInntektsmelding)) return false;
        ArbeidsforholdOgInntektsmelding that = (ArbeidsforholdOgInntektsmelding) o;
        return Objects.equals(arbeidsforhold, that.arbeidsforhold);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsforhold);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(ArbeidsforholdOgInntektsmelding arbeidsforholdOgInntektsmelding) {
        return new Builder(arbeidsforholdOgInntektsmelding);
    }

    public static class Builder {
        private final ArbeidsforholdOgInntektsmelding kladd;

        private Builder() {
            kladd = new ArbeidsforholdOgInntektsmelding();
        }

        private Builder(ArbeidsforholdOgInntektsmelding arbeidsforholdOgInntektsmelding) {
            kladd = arbeidsforholdOgInntektsmelding;
        }

        public Builder medArbeidsforhold(Arbeidsforhold arbeidsforhold) {
            kladd.arbeidsforhold = arbeidsforhold;
            return this;
        }

        public Builder medRefusjonskrav(List<Refusjonskrav> refusjoner) {
            kladd.refusjoner = refusjoner;
            return this;
        }

        public Builder medGyldigeRefusjonskrav(List<Refusjonskrav> gyldigeRefusjonskrav) {
            kladd.gyldigeRefusjonskrav = gyldigeRefusjonskrav;
            return this;
        }

        public Builder medGraderinger(List<Gradering> graderinger) {
            kladd.graderinger = graderinger;
            return this;
        }

        public Builder medRefusjonskravFrist(RefusjonskravFrist refusjonskravFrist) {
            kladd.refusjonskravFrist = refusjonskravFrist;
            return this;
        }

        public Builder medNaturalytelser(List<NaturalYtelse> naturalYtelser) {
            kladd.naturalYtelser = naturalYtelser;
            return this;
        }

        public Builder medAnsettelsesperiode(Periode ansettelsesperiode) {
            kladd.ansettelsesperiode = ansettelsesperiode;
            return this;
        }

        public Builder medStartdatoPermisjon(LocalDate startdatoPermisjon) {
            kladd.startdatoPermisjon = startdatoPermisjon;
            return this;
        }

        public Builder medAndelsnr(Long andelsnr) {
            kladd.andelsnr = andelsnr;
            return this;
        }

        public Builder medInnsendingsdatoFørsteInntektsmeldingMedRefusjon(LocalDate innsendingsdatoFørsteInntektsmeldingMedRefusjon) {
            kladd.innsendingsdatoFørsteInntektsmeldingMedRefusjon = innsendingsdatoFørsteInntektsmeldingMedRefusjon;
            return this;
        }

        public Builder medOverstyrtRefusjonsFrist(LocalDate overstyrtRefusjonsFrist) {
            kladd.overstyrtRefusjonsFrist = overstyrtRefusjonsFrist;
            return this;
        }

        public ArbeidsforholdOgInntektsmelding build() {
            return kladd;
        }
    }

    @Override
    public String toString() {
        return "ArbeidsforholdOgInntektsmelding{" +
            "arbeidsforhold=" + arbeidsforhold +
            ", refusjoner=" + refusjoner +
            ", gyldigeRefusjonskrav=" + gyldigeRefusjonskrav +
            ", graderinger=" + graderinger +
            ", naturalYtelser=" + naturalYtelser +
            ", ansettelsesperiode=" + ansettelsesperiode +
            ", startdatoPermisjon=" + startdatoPermisjon +
            ", andelsnr=" + andelsnr +
            ", innsendingsdatoFørsteInntektsmeldingMedRefusjon=" + innsendingsdatoFørsteInntektsmeldingMedRefusjon +
            ", overstyrtRefusjonsFrist=" + overstyrtRefusjonsFrist +
            '}';
    }
}
