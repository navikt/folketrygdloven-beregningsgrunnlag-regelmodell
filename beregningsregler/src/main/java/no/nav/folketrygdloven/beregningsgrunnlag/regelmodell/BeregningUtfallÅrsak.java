package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell;

import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsakKoder.AVSLAG;
import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsakKoder.AVVIK_25;
import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsakKoder.AVVIK_25_TIDBEGRENSET;
import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsakKoder.AVVIK_25_VARIG_ENDRET_NAERING;
import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsakKoder.AVVIK_25_VARIG_ENDRING_MIDLERTIDIG_INAKTIV;
import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsakKoder.SN_NY_I_ARBEIDSLIVET;

public enum BeregningUtfallÅrsak {

	AVSLAG_UNDER_HALV_G(AVSLAG, "Avslag beregningsvilkår, brutto årsbeløp under 0.5G"),
	AVSLAG_UNDER_TREKVART_G(AVSLAG, "Avslag beregningsvilkår, brutto årsbeløp under 0.75G"),
	AVSLAG_UNDER_EN_G(AVSLAG, "Avslag beregningsvilkår, brutto årsbeløp under 1G"),
	FASTSETT_AVVIK_OVER_25_PROSENT(AVVIK_25, "Avvik {0}% er > 25%, beregningsgrunnlag fastsettes ved skjønn"),
	FASTSETT_AVVIK_OVER_25_PROSENT_ARBEIDSTAKER(AVVIK_25, "Avvik {0}% for AT er > 25%, beregningsgrunnlag fastsettes ved skjønn"),
	FASTSETT_AVVIK_OVER_25_PROSENT_FRILANS(AVVIK_25, "Avvik {0}% for FL er > 25%, beregningsgrunnlag fastsettes ved skjønn"),
	FASTSETT_AVVIK_TIDSBEGRENSET_ARBEIDSTAKER(AVVIK_25, "Avvik {0}% er > 25% og bruker har tidsbegrenset arbeidsforhold i foregående periode, beregningsgrunnlag fastsettes ved skjønn"),
	FASTSETT_AVVIK_TIDSBEGRENSET(AVVIK_25_TIDBEGRENSET, "Avvik {0}% er > 25% og bruker har tidsbegrenset arbeidsforhold i foregående periode, beregningsgrunnlag fastsettes ved skjønn"),
	FASTSETT_SELVSTENDIG_NY_ARBEIDSLIVET(SN_NY_I_ARBEIDSLIVET, "Fastsett beregningsgrunnlag for selvstendig næringsdrivende som er ny i arbeidslivet"),
	VARIG_ENDRING_OG_AVVIK_STØRRE_ENN_25_PROSENT(AVVIK_25_VARIG_ENDRET_NAERING, "Varig endring og avvik større enn 25%, beregningsgrunnlag fastsettes skjønnsmessig"),
	VARIG_ENDRING_OG_AVVIK_STØRRE_ENN_25_PROSENT_MIDLERTIDIG_INAKTIV(AVVIK_25_VARIG_ENDRING_MIDLERTIDIG_INAKTIV, "Varig endring og avvik større enn 25%, beregningsgrunnlag fastsettes skjønnsmessig"),

	FRISINN_FRILANS_UTEN_INNTEKT("FRILANS_UTEN_INNTEKT", "Avslag grunnet frilans uten inntekt"),
	UDEFINERT("x", "y");


	private String navn;

	private String kode;

	BeregningUtfallÅrsak(String kode, String navn) {
		this.kode = kode;
		this.navn = navn;
	}

	public String getNavn() {
		return navn;
	}

	public String getKode() {
		return kode;
	}


}
