package service;

import model.*;
import org.junit.jupiter.api.Test;
import javax.print.*;
import javax.print.attribute.standard.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PrinterServiceTest {
    private PrintService printer(String name) {
        PrintService p = mock(PrintService.class); when(p.getName()).thenReturn(name); return p;
    }
    private ProductionRequest valid() {
        ProductionRequest r = new ProductionRequest(); r.setProductName("Café"); r.setQuantity(3);
        r.setDataPreparacao("2026-10-02"); r.setDataValidade("2026-10-02");
        r.setHorarioPreparo("10:00"); r.setHorarioDescarte("12:00"); r.setLabelType(PrintRequest.LabelType.SIXTY_TWO_MM); return r;
    }
    @Test void selectsConfiguredPrinterEvenWhenAnotherIsFirst() throws Exception {
        PrintService zebra = printer("Zebra"); PrinterService.Sender sender = mock(PrinterService.Sender.class);
        PrinterService service = new PrinterService(() -> "Zebra", () -> new PrintService[]{printer("Office"), zebra}, sender);
        service.printProductionLabel(valid());
        verify(sender).send(eq(zebra), argThat(zpl -> zpl.split("\\^XA", -1).length == 4 && zpl.contains("Café")));
    }
    @Test void missingOrBlankPrinterDoesNotSend() {
        for (String name : new String[]{"", "missing"}) {
            PrinterService.Sender sender = mock(PrinterService.Sender.class);
            PrinterService service = new PrinterService(() -> name, () -> new PrintService[]{printer("Office")}, sender);
            assertThrows(PrinterService.PrinterServiceException.class, () -> service.printProductionLabel(valid()));
            verifyNoInteractions(sender);
        }
    }
    @Test void acceptsInternalCupsQueueName() {
        PrintService p = printer("Nome amigável");
        when(p.getAttribute(PrinterName.class)).thenReturn(new PrinterName("Zebra_internal", null));
        assertSame(p, new PrinterService(() -> "Zebra_internal", () -> new PrintService[]{p}, null).selectPrinter());
    }
    @Test void stoppedOrRejectingPrinterDoesNotSend() {
        PrintService p = printer("Zebra"); PrinterService.Sender sender = mock(PrinterService.Sender.class);
        PrinterService service = new PrinterService(() -> "Zebra", () -> new PrintService[]{p}, sender);
        when(p.getAttribute(PrinterState.class)).thenReturn(PrinterState.STOPPED);
        assertThrows(PrinterService.PrinterServiceException.class, () -> service.printProductionLabel(valid()));
        when(p.getAttribute(PrinterState.class)).thenReturn(PrinterState.IDLE);
        when(p.getAttribute(PrinterIsAcceptingJobs.class)).thenReturn(PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS);
        assertThrows(PrinterService.PrinterServiceException.class, () -> service.printProductionLabel(valid()));
        verifyNoInteractions(sender);
    }
    @Test void retainsCauseAndInterruptFlag() {
        PrintService p = printer("Zebra"); InterruptedException cause = new InterruptedException("transport");
        PrinterService service = new PrinterService(() -> "Zebra", () -> new PrintService[]{p}, (printer, zpl) -> { throw cause; });
        try {
            var failure = assertThrows(PrinterService.PrinterServiceException.class, () -> service.printProductionLabel(valid()));
            assertSame(cause, failure.getCause()); assertTrue(Thread.currentThread().isInterrupted());
        } finally { Thread.interrupted(); }
    }
    @Test void invalidProductionNeverTouchesPrinter() {
        ProductionRequest r = valid(); r.setLabelType(PrintRequest.LabelType.STANDARD);
        PrinterService.Sender sender = mock(PrinterService.Sender.class);
        PrinterService service = new PrinterService(() -> { fail("Printer should not be looked up"); return ""; }, () -> new PrintService[0], sender);
        assertThrows(validation.RequestValidationException.class, () -> service.printProductionLabel(r)); verifyNoInteractions(sender);
    }
    @Test void sequenceFailurePreventsAnySend() {
        PrintService p = printer("Zebra"); PrinterService.Sender sender = mock(PrinterService.Sender.class);
        PrintRequest r = new PrintRequest(); r.setText("Pão"); r.setSetor("Cozinha"); r.setQuantity(1);
        r.setDataFabricacao("2026-10-02"); r.setDataValidade("2026-10-02"); r.setLabelType(PrintRequest.LabelType.SIXTY_TWO_MM);
        try (var sequence = mockStatic(SequenceManager.class)) {
            sequence.when(() -> SequenceManager.getNextSequenceAndIncrement(1)).thenThrow(new SequenceException("disk failure"));
            assertThrows(SequenceException.class, () -> new PrinterService(() -> "Zebra", () -> new PrintService[]{p}, sender).printLabels(r));
            verifyNoInteractions(sender);
        }
    }
}
