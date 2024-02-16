package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.SammenligningGrunnlagType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp.OmsorgspengerGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;

@RuleDocumentationGrunnlag
public class BeregningsgrunnlagPeriode {
	@JsonManagedReference
	private List<BeregningsgrunnlagPrStatus> beregningsgrunnlagPrStatus = new ArrayList<>();
	private Periode bgPeriode;
	private List<PeriodeÅrsak> periodeÅrsaker = new ArrayList<>();
	@JsonBackReference
	private Beregningsgrunnlag beregningsgrunnlag;
	private boolean erVilkårOppfylt = true;


	private BeregningsgrunnlagPeriode() {
	}

	public BeregningsgrunnlagPrStatus getBeregningsgrunnlagPrStatus(AktivitetStatus aktivitetStatus) {
		return getBeregningsgrunnlagPrStatus().stream()
				.filter(af -> aktivitetStatus.equals(af.getAktivitetStatus()))
				.findFirst()
				.orElse(null);
	}

	public List<BeregningsgrunnlagPrStatus> getBeregningsgrunnlagPrStatuser(AktivitetStatus aktivitetStatus) {
		return getBeregningsgrunnlagPrStatus().stream()
				.filter(af -> aktivitetStatus.equals(af.getAktivitetStatus()))
				.toList();
	}

	public Optional<BeregningsgrunnlagPrStatus> getBeregningsgrunnlagFraDagpenger() {
		return getBeregningsgrunnlagPrStatus().stream()
				.filter(af -> af.getAktivitetStatus().erDP())
				.findFirst();
	}

	@JsonIgnore
	public Beregningsgrunnlag getBeregningsgrunnlag() {
		return beregningsgrunnlag;
	}

	void setBeregningsgrunnlag(Beregningsgrunnlag beregningsgrunnlag) {
		this.beregningsgrunnlag = beregningsgrunnlag;
	}

	void addBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus beregningsgrunnlagPrStatus) {
		Objects.requireNonNull(beregningsgrunnlagPrStatus, "beregningsgrunnlagPrStatus");
		Objects.requireNonNull(beregningsgrunnlagPrStatus.getAktivitetStatus(), "aktivitetStatus");
		this.beregningsgrunnlagPrStatus.add(beregningsgrunnlagPrStatus);
	}

	public BigDecimal getBruttoPrÅr() {
		return getBeregningsgrunnlagPrStatus().stream()
				.map(BeregningsgrunnlagPrStatus::getBruttoPrÅr)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public BigDecimal getAvkortetPrÅr() {
		return getBeregningsgrunnlagPrStatus().stream()
				.map(BeregningsgrunnlagPrStatus::getAvkortetPrÅr)
				.filter(Objects::nonNull)
				.reduce(BigDecimal::add)
				.orElse(null);
	}

	public BigDecimal getRedusertPrÅr() {
		return beregningsgrunnlagPrStatus.stream()
				.map(BeregningsgrunnlagPrStatus::getRedusertPrÅr)
				.filter(Objects::nonNull)
				.reduce(BigDecimal::add)
				.orElse(null);
	}

	public Periode getBeregningsgrunnlagPeriode() {
		return bgPeriode;
	}

	public LocalDate getPeriodeFom() {
		return bgPeriode.getFom();
	}

	public LocalDate getPeriodeTom() {
		return bgPeriode.getTom();
	}

	public Collection<BeregningsgrunnlagPrStatus> getBeregningsgrunnlagPrStatus() {
		return Collections.unmodifiableCollection(beregningsgrunnlagPrStatus);
	}

	public Inntektsgrunnlag getInntektsgrunnlag() {
		return beregningsgrunnlag.getInntektsgrunnlag();
	}

	public LocalDate getSkjæringstidspunkt() {
		return beregningsgrunnlag.getSkjæringstidspunkt();
	}

	public BigDecimal getGrunnbeløp() {
		return beregningsgrunnlag.getGrunnbeløp();
	}

	public BigDecimal getUregulertGrunnbeløp() {
		return beregningsgrunnlag.getUregulertGrunnbeløp();
	}

	public Optional<SammenligningsGrunnlag> getSammenligningsGrunnlagForType(SammenligningGrunnlagType type) {
		return beregningsgrunnlag.getSammenligningsgrunnlagForStatus(type);
	}

	public SammenligningsGrunnlag getSammenligningsGrunnlagForTypeEllerFeil(SammenligningGrunnlagType type) {
		return beregningsgrunnlag.getSammenligningsgrunnlagForStatus(type)
				.orElseThrow(() -> new IllegalStateException("FEIL: Forventet å finne sammenligningsgrunnlag for type " + type + " men denne var ikke satt"));
	}

	public List<AktivitetStatusMedHjemmel> getAktivitetStatuser() {
		return beregningsgrunnlag.getAktivitetStatuser().stream()
				.sorted(Comparator.comparing(as -> as.getAktivitetStatus().getBeregningPrioritet()))
				.toList();
	}

	public List<PeriodeÅrsak> getPeriodeÅrsaker() {
		return periodeÅrsaker;
	}

	public BigDecimal getAvviksgrenseProsent() {
		return getBeregningsgrunnlag().getAvviksgrenseProsent();
	}

	public BigDecimal getYtelsedagerPrÅr() {
		return getBeregningsgrunnlag().getYtelsedagerPrÅr();
	}


	public boolean skalSjekkeUtbetalingTilBrukerFørAvviksvurdering() {
		YtelsesSpesifiktGrunnlag ytelsesSpesifiktGrunnlag = beregningsgrunnlag.getYtelsesSpesifiktGrunnlag();
		return ytelsesSpesifiktGrunnlag instanceof OmsorgspengerGrunnlag;
	}

	public boolean erBesteberegnet() {
		return getBeregningsgrunnlag().erBesteberegnet();
	}

	public boolean getErVilkårOppfylt() {
		return erVilkårOppfylt;
	}

	public boolean skalSplitteSammenligningsgrunnlagToggle() {
		return beregningsgrunnlag.isSplitteATFLToggleErPå();
	}

	public static Builder builder() {
		return new Builder();
	}

	// FIXME: Dette er en skjult mutator. Endre navn eller pattern?
	public static Builder builder(BeregningsgrunnlagPeriode eksisterendeBeregningsgrunnlagPeriode) {
		return new Builder(eksisterendeBeregningsgrunnlagPeriode);
	}

	public BigDecimal getMinsteinntektMilitærHarKravPå() {
		return beregningsgrunnlag.getMinsteinntektMilitærHarKravPå();
	}

	public Optional<LocalDate> getFomDatoForIndividuellSammenligningATFLSN() {
		return beregningsgrunnlag.getFomDatoForIndividuellSammenligningATFLSN();
	}

	public static class Builder {
		private BeregningsgrunnlagPeriode beregningsgrunnlagPeriodeMal;

		private Builder() {
			beregningsgrunnlagPeriodeMal = new BeregningsgrunnlagPeriode();
		}

		public Builder(BeregningsgrunnlagPeriode eksisterendeBeregningsgrunnlagPeriod) {
			beregningsgrunnlagPeriodeMal = eksisterendeBeregningsgrunnlagPeriod;
		}

		public Builder medPeriode(Periode beregningsgrunnlagPeriode) {
			beregningsgrunnlagPeriodeMal.bgPeriode = beregningsgrunnlagPeriode;
			return this;
		}

		public Builder leggTilPeriodeÅrsak(PeriodeÅrsak periodeÅrsak) {
			if (!(beregningsgrunnlagPeriodeMal.periodeÅrsaker instanceof ArrayList)) {
				beregningsgrunnlagPeriodeMal.periodeÅrsaker = new ArrayList<>(beregningsgrunnlagPeriodeMal.periodeÅrsaker);
			}
			if (!beregningsgrunnlagPeriodeMal.periodeÅrsaker.contains(periodeÅrsak)) {
				beregningsgrunnlagPeriodeMal.periodeÅrsaker.add(periodeÅrsak);
			}
			return this;
		}

		public Builder medPeriodeÅrsaker(List<PeriodeÅrsak> periodeÅrsaker) {
			beregningsgrunnlagPeriodeMal.periodeÅrsaker = periodeÅrsaker;
			return this;
		}


		public Builder medErVilkårOppfylt(boolean erVilkårOppfylt) {
			beregningsgrunnlagPeriodeMal.erVilkårOppfylt = erVilkårOppfylt;
			return this;
		}

		public Builder leggTilPeriodeÅrsaker(List<PeriodeÅrsak> periodeÅrsaker) {
			periodeÅrsaker.forEach(this::leggTilPeriodeÅrsak);
			return this;
		}

		public Builder medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus beregningsgrunnlagPrStatus) {
			if (beregningsgrunnlagPrStatus.getAndelNr() != null && beregningsgrunnlagPeriodeMal.beregningsgrunnlagPrStatus.stream()
					.anyMatch(bps -> beregningsgrunnlagPrStatus.getAndelNr().equals(bps.getAndelNr()))) {
				throw new IllegalArgumentException("AndelNr er null eller finnes allerede: " + beregningsgrunnlagPrStatus.getAndelNr());
			}
			beregningsgrunnlagPeriodeMal.addBeregningsgrunnlagPrStatus(beregningsgrunnlagPrStatus);
			beregningsgrunnlagPrStatus.setBeregningsgrunnlagPeriode(beregningsgrunnlagPeriodeMal);
			return this;
		}

		public BeregningsgrunnlagPeriode build() {
			verifyStateForBuild();
			return beregningsgrunnlagPeriodeMal;
		}

		private void verifyStateForBuild() {
			Objects.requireNonNull(beregningsgrunnlagPeriodeMal.beregningsgrunnlagPrStatus, "beregningsgrunnlagPrStatus");
			Objects.requireNonNull(beregningsgrunnlagPeriodeMal.bgPeriode, "bgPeriode");
			Objects.requireNonNull(beregningsgrunnlagPeriodeMal.bgPeriode.getFom(), "bgPeriode.getFom()");
		}
	}
}
