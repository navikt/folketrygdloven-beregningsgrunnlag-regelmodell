package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import no.nav.folketrygdloven.kalkulator.modell.typer.FaktaVurdering;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaVurderingKilde;

public class FaktaAktørDto {

    private FaktaVurdering erNyIArbeidslivetSN;
    private FaktaVurdering erNyoppstartetFL;
    private FaktaVurdering harFLMottattYtelse;
    private FaktaVurdering skalBeregnesSomMilitær;
    private FaktaVurdering skalBesteberegnes;
    private FaktaVurdering mottarEtterlønnSluttpakke;

    public FaktaAktørDto() { }

    public FaktaAktørDto(FaktaAktørDto original) {
        this.erNyIArbeidslivetSN = original.getErNyIArbeidslivetSN();
        this.erNyoppstartetFL = original.getErNyoppstartetFL();
        this.harFLMottattYtelse = original.getHarFLMottattYtelse();
        this.skalBesteberegnes = original.getSkalBesteberegnes()    ;
        this.mottarEtterlønnSluttpakke = original.getMottarEtterlønnSluttpakke();
        this.skalBeregnesSomMilitær = original.getSkalBeregnesSomMilitær();
    }

    public Boolean getErNyIArbeidslivetSNVurdering() {
        return finnVurdering(erNyIArbeidslivetSN);
    }

    public Boolean getErNyoppstartetFLVurdering() {
        return finnVurdering(erNyoppstartetFL);
    }

    public Boolean getHarFLMottattYtelseVurdering() {
        return finnVurdering(harFLMottattYtelse);
    }

    public Boolean getSkalBesteberegnesVurdering() {
        return finnVurdering(skalBesteberegnes);
    }

    public Boolean getMottarEtterlønnSluttpakkeVurdering() {
        return finnVurdering(mottarEtterlønnSluttpakke);
    }

    public Boolean getSkalBeregnesSomMilitærVurdering() {
        return finnVurdering(skalBeregnesSomMilitær);
    }

    public FaktaVurdering getErNyIArbeidslivetSN() {
        return erNyIArbeidslivetSN;
    }

    public FaktaVurdering getErNyoppstartetFL() {
        return erNyoppstartetFL;
    }

    public FaktaVurdering getHarFLMottattYtelse() {
        return harFLMottattYtelse;
    }

    public FaktaVurdering getSkalBeregnesSomMilitær() {
        return skalBeregnesSomMilitær;
    }

    public FaktaVurdering getSkalBesteberegnes() {
        return skalBesteberegnes;
    }

    public FaktaVurdering getMottarEtterlønnSluttpakke() {
        return mottarEtterlønnSluttpakke;
    }

    private Boolean finnVurdering(FaktaVurdering vurdering) {
        return vurdering == null ? null : vurdering.getVurdering();
    }

    @Override
    public String toString() {
        return "FaktaAktørDto{" +
                "erNyIArbeidslivetSN=" + erNyIArbeidslivetSN +
                ", erNyoppstartetFL=" + erNyoppstartetFL +
                ", harFLMottattYtelse=" + harFLMottattYtelse +
                ", skalBesteberegnes=" + skalBesteberegnes +
                ", mottarEtterlønnSluttpakke=" + mottarEtterlønnSluttpakke +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(FaktaAktørDto kopi) {
        return new Builder(kopi);
    }

    public static class Builder {
        private FaktaAktørDto mal;

        private Builder() {
            mal = new FaktaAktørDto();
        }

        private Builder(FaktaAktørDto faktaAktørDto) {
            mal = new FaktaAktørDto(faktaAktørDto);
        }

        static Builder oppdater(FaktaAktørDto faktaAktørDto) {
            return faktaAktørDto == null ? new Builder() : new Builder(faktaAktørDto);
        }

        public Builder medErNyIArbeidslivetSN(FaktaVurdering erNyIArbeidslivetSN) {
            mal.erNyIArbeidslivetSN = erNyIArbeidslivetSN;
            return this;
        }

        public Builder medErNyoppstartetFL(FaktaVurdering erNyoppstartetFL) {
            mal.erNyoppstartetFL = erNyoppstartetFL;
            return this;
        }

        public Builder medHarFLMottattYtelse(FaktaVurdering harFLMottattYtelse) {
            mal.harFLMottattYtelse = harFLMottattYtelse;
            return this;
        }

        public Builder medSkalBesteberegnes(FaktaVurdering skalBesteberegnes) {
            mal.skalBesteberegnes = skalBesteberegnes;
            return this;
        }

        public Builder medErMilitærSiviltjeneste(FaktaVurdering skalBeregnesSomMilitær) {
            mal.skalBeregnesSomMilitær = skalBeregnesSomMilitær;
            return this;
        }


        public Builder medMottarEtterlønnSluttpakke(FaktaVurdering mottarEtterlønnSluttpakke) {
            mal.mottarEtterlønnSluttpakke = mottarEtterlønnSluttpakke;
            return this;
        }

        public Builder medErNyIArbeidslivetSNFastsattAvSaksbehandler(Boolean erNyIArbeidslivetSN) {
            mal.erNyIArbeidslivetSN = new FaktaVurdering(erNyIArbeidslivetSN, FaktaVurderingKilde.SAKSBEHANDLER);
            return this;
        }

        public Builder medErNyoppstartetFLFastsattAvSaksbehandler(Boolean erNyoppstartetFL) {
            mal.erNyoppstartetFL = new FaktaVurdering(erNyoppstartetFL, FaktaVurderingKilde.SAKSBEHANDLER);
            return this;
        }

        public Builder medHarFLMottattYtelseFastsattAvSaksbehandler(Boolean harFLMottattYtelse) {
            mal.harFLMottattYtelse = new FaktaVurdering(harFLMottattYtelse, FaktaVurderingKilde.SAKSBEHANDLER);
            return this;
        }

        public Builder medSkalBesteberegnesFastsattAvSaksbehandler(Boolean skalBesteberegnes) {
            mal.skalBesteberegnes = new FaktaVurdering(skalBesteberegnes, FaktaVurderingKilde.SAKSBEHANDLER);
            return this;
        }

        public Builder medErMilitærSiviltjenesteFastsattAvSaksbehandler(Boolean skalBeregnesSomMilitær) {
            mal.skalBeregnesSomMilitær = new FaktaVurdering(skalBeregnesSomMilitær, FaktaVurderingKilde.SAKSBEHANDLER);
            return this;
        }


        public Builder medMottarEtterlønnSluttpakkeFastsattAvSaksbehandler(Boolean mottarEtterlønnSluttpakke) {
            mal.mottarEtterlønnSluttpakke = new FaktaVurdering(mottarEtterlønnSluttpakke, FaktaVurderingKilde.SAKSBEHANDLER);
            return this;
        }

        public FaktaAktørDto build() {
            verifyStateForBuild();
            return mal;
        }

        private void verifyStateForBuild() {
            if (erUgyldig()) {
                throw new IllegalStateException("Må ha satt minst et faktafelt.");
            }
        }

        // Brukes av fp-sak og må vere public
        public boolean erUgyldig() {
            return mal.erNyIArbeidslivetSN == null
                    && mal.erNyoppstartetFL == null
                    && mal.skalBeregnesSomMilitær == null
                    && mal.harFLMottattYtelse == null
                    && mal.skalBesteberegnes == null
                    && mal.mottarEtterlønnSluttpakke == null;
        }

    }
}
