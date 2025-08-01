package service.strategies;

/**
 * Define o contrato para todas as estratégias de geração de ZPL.
 */
public interface ILabelStrategy {
    String generateZpl();
}