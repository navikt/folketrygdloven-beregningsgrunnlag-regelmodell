package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell;

import java.util.Objects;

public record RegelMerknad(String merknadKode,
                           BeregningUtfallÅrsak utfallÅrsak,
                           @Deprecated(forRemoval = true) String merknadTekst) {

	// Foretrukket
	public RegelMerknad(BeregningUtfallÅrsak utfallÅrsak) {
		this(utfallÅrsak.getKode(), utfallÅrsak, "");
	}

	@Deprecated // inntill ryddet i kalkulus
	public RegelMerknad(String merknadKode, String merknadTekst) {
		this(merknadKode, BeregningUtfallÅrsak.UDEFINERT, merknadTekst);
	}

    @Deprecated // Bruk utfallÅrsak() eller merknadKode()
	public String getMerknadKode() {
        return merknadKode;
    }

	@Deprecated // BrukmerknadTekst()
    public String getMerknadTekst() {
        return merknadTekst;
    }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof RegelMerknad)) return false;
		RegelMerknad that = (RegelMerknad) o;
		return Objects.equals(merknadKode, that.merknadKode) && utfallÅrsak == that.utfallÅrsak;
	}

	@Override
	public int hashCode() {
		return Objects.hash(merknadKode, utfallÅrsak);
	}
}
