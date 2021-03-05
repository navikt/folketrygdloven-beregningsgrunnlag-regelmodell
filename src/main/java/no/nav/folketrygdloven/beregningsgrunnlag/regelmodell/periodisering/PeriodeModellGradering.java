package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;

public class PeriodeModellGradering implements PeriodeModell {
    private LocalDate skjæringstidspunkt;
    private BigDecimal grunnbeløp;
    private List<AndelEndring> andelGraderinger = new ArrayList<>();
    private List<SplittetPeriode> eksisterendePerioder = new ArrayList<>();
    private List<PeriodisertBruttoBeregningsgrunnlag> periodisertBruttoBeregningsgrunnlagList = Collections.emptyList();

    public LocalDate getSkjæringstidspunkt() {
        return skjæringstidspunkt;
    }

    public BigDecimal getGrunnbeløp() {
        return grunnbeløp;
    }

	@Override
	public List<AndelEndring> getEndringListeForSplitting() {
		return andelGraderinger;
	}

	public List<AndelEndring> getAndelGraderinger() {
        return andelGraderinger;
    }

    public List<SplittetPeriode> getEksisterendePerioder() {
        return eksisterendePerioder;
    }

    public List<PeriodisertBruttoBeregningsgrunnlag> getPeriodisertBruttoBeregningsgrunnlagList() {
        return periodisertBruttoBeregningsgrunnlagList;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final PeriodeModellGradering kladd;

        public Builder() {
            kladd = new PeriodeModellGradering();
        }

        public Builder medSkjæringstidspunkt(LocalDate skjæringstidspunkt) {
            kladd.skjæringstidspunkt = skjæringstidspunkt;
            return this;
        }

        public Builder medGrunnbeløp(BigDecimal grunnbeløp) {
            kladd.grunnbeløp = grunnbeløp;
            return this;
        }

        public Builder medAndelGraderinger(List<AndelEndring> andelGraderinger) {
            kladd.andelGraderinger = andelGraderinger;
            return this;
        }

        public Builder medEksisterendePerioder(List<SplittetPeriode> eksisterendePerioder) {
            kladd.eksisterendePerioder = eksisterendePerioder;
            return this;
        }

        public Builder medPeriodisertBruttoBeregningsgrunnlag(List<PeriodisertBruttoBeregningsgrunnlag> periodiseringBruttoBg) {
            kladd.periodisertBruttoBeregningsgrunnlagList = periodiseringBruttoBg;
            return this;
        }

        public PeriodeModellGradering build() {
            return kladd;
        }
    }
}
