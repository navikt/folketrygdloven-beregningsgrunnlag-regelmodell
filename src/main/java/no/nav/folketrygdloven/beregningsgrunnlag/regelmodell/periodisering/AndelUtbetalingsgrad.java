package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Gradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetAndel;

public class AndelUtbetalingsgrad implements AndelEndring {
    private AktivitetStatusV2 aktivitetStatus;
    private List<Gradering> graderinger = new ArrayList<>();
    private Arbeidsforhold arbeidsforhold;
    private Long andelsnr;

    private AndelUtbetalingsgrad() {
    }

	@Override
	public boolean filterForEksisterendeAktiviteter() {
		return false;
	}

    @Override
    public AktivitetStatusV2 getAktivitetStatus() {
        return aktivitetStatus;
    }

    @Override
    public List<Gradering> getGraderinger() {
        return graderinger;
    }

    @Override
    public boolean erNyAktivitet(PeriodeModell input, LocalDate periodeFom) {
        return andelsnr == null;
    }

    @Override
    public List<Refusjonskrav> getGyldigeRefusjonskrav() {
        return Collections.emptyList();
    }

    @Override
    public Arbeidsforhold getArbeidsforhold() {
        return arbeidsforhold;
    }

	@Override
	public boolean filterForNyeAktiviteter(LocalDate skjæringstidspunkt, LocalDate periodeFom) {
		return harUtbetalingIPeriode(periodeFom);
	}

	private boolean harUtbetalingIPeriode(LocalDate periodeFom) {
		return getGraderinger().stream().anyMatch(g -> g.getPeriode().inneholder(periodeFom) && g.getUtbetalingsprosent().compareTo(BigDecimal.ZERO) > 0);
	}
	@Override
	public EksisterendeAndel mapForEksisterendeAktiviteter(LocalDate fom) {
		return EksisterendeAndel.builder()
				.medAndelNr(andelsnr)
				.medArbeidsforhold(getArbeidsforhold())
				.build();
	}


	@Override
	public SplittetAndel mapForNyeAktiviteter(LocalDate periodeFom) {
		return mapSplittetAndel();
	}

	private SplittetAndel mapSplittetAndel() {
		SplittetAndel.Builder builder = SplittetAndel.builder()
				.medAktivitetstatus(getAktivitetStatus())
				.medArbeidsforhold(getArbeidsforhold());
		return builder.build();
	}

	public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AndelUtbetalingsgrad kladd;

        private Builder() {
            kladd = new AndelUtbetalingsgrad();
        }

        public Builder medAktivitetStatus(AktivitetStatusV2 aktivitetStatus) {
            kladd.aktivitetStatus = aktivitetStatus;
            return this;
        }

        public Builder medGraderinger(List<Gradering> graderinger) {
            kladd.graderinger = graderinger;
            return this;
        }

        public Builder medAndelsnr(Long andelsnr) {
            kladd.andelsnr = andelsnr;
            return this;
        }

        public Builder medArbeidsforhold(Arbeidsforhold arbeidsforhold) {
            kladd.arbeidsforhold = arbeidsforhold;
            return this;
        }

        public AndelUtbetalingsgrad build() {
            return kladd;
        }
    }

    @Override
    public String toString() {
        return "AndelGraderingImpl{" +
            "aktivitetStatus=" + aktivitetStatus +
            ", graderinger=" + graderinger +
            ", arbeidsforhold=" + arbeidsforhold +
            ", andelsnr=" + andelsnr +
            '}';
    }
}
