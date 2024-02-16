package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;

/**
 * Samling av ulike konstanter og toggler som brukes i reglene
 */
public class Konstanter {

    // Konstanter

    private int antallGMilitærHarKravPå = 3;
    private BigDecimal antallGØvreGrenseverdi = BigDecimal.valueOf(6);
	private BigDecimal midlertidigInaktivTypeAReduksjonsfaktor = BigDecimal.valueOf(0.65);
	private BigDecimal ytelsedagerIPrÅr = BigDecimal.valueOf(260);
    private BigDecimal avviksgrenseProsent = BigDecimal.valueOf(25);
    private List<Grunnbeløp> grunnbeløpSatser = new ArrayList<>();

	private boolean splitteATFLToggleErPå = false;

	/**
	 * https://jira.adeo.no/browse/TFP-5171
	 * Bør gjøres obligatorisk når tatt i bruk i kalkulus
	 */
	private LocalDate fomDatoForIndividuellSammenligningATFLSN;


    void setAntallGMilitærHarKravPå(int antallGMilitærHarKravPå) {
        this.antallGMilitærHarKravPå = antallGMilitærHarKravPå;
    }

	public void setAntallGØvreGrenseverdi(BigDecimal antallGØvreGrenseverdi) {
        this.antallGØvreGrenseverdi = antallGØvreGrenseverdi;
    }

	public void setMidlertidigInaktivTypeAReduksjonsfaktor(BigDecimal midlertidigInaktivTypeAReduksjonsfaktor) {
		this.midlertidigInaktivTypeAReduksjonsfaktor = midlertidigInaktivTypeAReduksjonsfaktor;
	}

	public void setYtelsedagerIPrÅr(BigDecimal ytelsedagerIPrÅr) {
        this.ytelsedagerIPrÅr = ytelsedagerIPrÅr;
    }

	public Optional<LocalDate> getFomDatoForIndividuellSammenligningATFLSN() {
		return Optional.ofNullable(fomDatoForIndividuellSammenligningATFLSN);
	}

	public void setFomDatoForIndividuellSammenligningATFLSN(LocalDate fomDatoForIndividuellSammenligningATFLSN) {
		this.fomDatoForIndividuellSammenligningATFLSN = fomDatoForIndividuellSammenligningATFLSN;
	}

	public void setAvviksgrenseProsent(BigDecimal avviksgrenseProsent) {
        this.avviksgrenseProsent = avviksgrenseProsent;
    }

    public void setSplitteATFLToggleErPå(boolean splitteATFLToggleErPå) {
        this.splitteATFLToggleErPå = splitteATFLToggleErPå;
    }

    public void setGrunnbeløpSatser(List<Grunnbeløp> grunnbeløpSatser) {
        this.grunnbeløpSatser = grunnbeløpSatser;
    }

    public int getAntallGMilitærHarKravPå() {
        return antallGMilitærHarKravPå;
    }

    public BigDecimal getAntallGØvreGrenseverdi() {
        return antallGØvreGrenseverdi;
    }

    public BigDecimal getYtelsedagerIPrÅr() {
        return ytelsedagerIPrÅr;
    }

    public BigDecimal getAvviksgrenseProsent() {
        return avviksgrenseProsent;
    }

	public BigDecimal getMidlertidigInaktivTypeAReduksjonsfaktor() {
		return midlertidigInaktivTypeAReduksjonsfaktor;
	}

    public boolean isSplitteATFLToggleErPå() {
        return splitteATFLToggleErPå;
    }

	public List<Grunnbeløp> getGrunnbeløpSatser() {
        if (grunnbeløpSatser.isEmpty()) {
            throw new IllegalStateException("Prøver å hente ut grunnbeløpsatser uten at dette er mappet til regelmodell.");
        }
        return grunnbeløpSatser;
    }

}
