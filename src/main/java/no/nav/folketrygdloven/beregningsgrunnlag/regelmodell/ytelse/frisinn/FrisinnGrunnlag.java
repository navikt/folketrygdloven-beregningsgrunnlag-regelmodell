package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.frisinn;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;

public class FrisinnGrunnlag extends YtelsesSpesifiktGrunnlag {

    private List<FrisinnPeriode> frisinnPerioder;
    private List<Periode> søknadsperioder;
    private LocalDate skjæringstidspunktOpptjening;

    public FrisinnGrunnlag(List<FrisinnPeriode> frisinnPerioder, List<Periode> søknadsperioder, LocalDate skjæringstidspunktOpptjening) {
        super("FRISINN");
        Objects.requireNonNull(frisinnPerioder, "frisinnPerioder");
        Objects.requireNonNull(skjæringstidspunktOpptjening, "skjæringstidspunktOpptjening");
        Objects.requireNonNull(søknadsperioder, "søknadsperioder");
        this.søknadsperioder = søknadsperioder;
        this.frisinnPerioder = frisinnPerioder;
        this.skjæringstidspunktOpptjening = skjæringstidspunktOpptjening;
    }

    public LocalDate getSkjæringstidspunktOpptjening() {
        return skjæringstidspunktOpptjening;
    }

    public List<FrisinnPeriode> getFrisinnPerioder() {
        return frisinnPerioder;
    }

    public boolean søkerFrilansISøknadsperiode(LocalDate dato) {
        Optional<Periode> søknadsperiode = søknadsperioder.stream().filter(s -> s.inneholder(dato)).findFirst();
        return søknadsperiode.map(sp -> frisinnPerioder.stream().filter(fp -> sp.overlapper(fp.getPeriode()))
            .anyMatch(FrisinnPeriode::getSøkerYtelseFrilans)).orElse(false);
    }

    public boolean søkerNæringISøknadsperiode(LocalDate dato) {
        Optional<Periode> søknadsperiode = søknadsperioder.stream().filter(s -> s.inneholder(dato)).findFirst();
        return søknadsperiode.map(sp -> frisinnPerioder.stream().filter(fp -> sp.overlapper(fp.getPeriode()))
            .anyMatch(FrisinnPeriode::getSøkerYtelseNæring)).orElse(false);
    }

    public boolean søkerYtelseFrilans(LocalDate dato) {
        return frisinnPerioder.stream().anyMatch(p -> p.inneholderDato(dato) && p.getSøkerYtelseFrilans());
    }

    public boolean søkerYtelseNæring(LocalDate dato) {
        return frisinnPerioder.stream().anyMatch(p -> p.inneholderDato(dato) && p.getSøkerYtelseNæring());
    }

    public boolean søkerYtelseFrilans() {
        return frisinnPerioder.stream().anyMatch(FrisinnPeriode::getSøkerYtelseFrilans);
    }

    public boolean søkerYtelseNæring() {
        return frisinnPerioder.stream().anyMatch(FrisinnPeriode::getSøkerYtelseNæring);
    }

    @Override
    public BigDecimal finnAndelAvBeregnet(BigDecimal beregnetPrÅr, BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, BeregningsgrunnlagPeriode periode) {
        return beregnetPrÅr;
    }
}
