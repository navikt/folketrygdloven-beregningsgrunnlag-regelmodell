package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;

public interface PeriodeModell {

    LocalDate getSkjæringstidspunkt();

    BigDecimal getGrunnbeløp();

    List<AndelEndring> getEndringListeForSplitting();

    List<SplittetPeriode> getEksisterendePerioder();

    List<PeriodisertBruttoBeregningsgrunnlag> getPeriodisertBruttoBeregningsgrunnlagList();



}
