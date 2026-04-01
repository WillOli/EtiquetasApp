                                    package model;


                                    public class PrintRequest {
                                        private String text;
                                        private String sector; // ADICIONADO
                                        private int quantity;
                                        private LabelType labelType;

                                        public enum LabelType {
                                            STANDARD, SIXTY_TWO_MM
                                        }

                                        public PrintRequest() {

                                        }

                                        // Getters e Setters
                                        public String getText() { return text; }
                                        public void setText(String text) { this.text = text; }

                                        public String getSector() { return sector; } // ADICIONADO
                                        public void setSector(String sector) { this.sector = sector; } // ADICIONADO

                                        public int getQuantity() { return quantity; }
                                        public void setQuantity(int quantity) { this.quantity = quantity; }

                                        public LabelType getLabelType() { return labelType; }
                                        public void setLabelType(LabelType labelType) { this.labelType = labelType; }


                                        /**
                                         * ✅ CORREÇÃO: A lógica de conversão está DENTRO do setter.
                                         * Este método recebe a String do JSON e a converte para o Enum antes de atribuir.
                                         */
                                        public void setLabelType(String labelTypeStr) {
                                            LabelType tempType;
                                            try {
                                                if (labelTypeStr == null || labelTypeStr.trim().isEmpty()) {
                                                    tempType = LabelType.STANDARD;
                                                } else {
                                                    tempType = LabelType.valueOf(labelTypeStr.toUpperCase());
                                                }
                                            } catch (IllegalArgumentException e) {
                                                System.err.println("[AVISO] Valor de labelType inválido recebido: '" + labelTypeStr + "'. Usando padrão.");
                                                tempType = LabelType.STANDARD;
                                            }
                                            // Atribui o Enum convertido, não a String original
                                            this.labelType = tempType;
                                        }
                                    }