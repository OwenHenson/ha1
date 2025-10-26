package htw.berlin.prog2.ha1;

/**
 * Eine Klasse, die das Verhalten des Online Taschenrechners imitiert, welcher auf
 * https://www.online-calculator.com/ aufgerufen werden kann (ohne die Memory-Funktionen)
 * und dessen Bildschirm bis zu zehn Ziffern plus einem Dezimaltrennzeichen darstellen kann.
 * Enthält mit Absicht noch diverse Bugs oder unvollständige Funktionen.
 */
public class Calculator {

    private String screen = "0";

    private double latestValue;

    private String latestOperation = "";

    /**
     * @return den aktuellen Bildschirminhalt als String
     */
    public String readScreen() {
        return screen;
    }

    /**
     * Empfängt den Wert einer gedrückten Zifferntaste. Da man nur eine Taste auf einmal
     * drücken kann muss der Wert positiv und einstellig sein und zwischen 0 und 9 liegen.
     * Führt in jedem Fall dazu, dass die gerade gedrückte Ziffer auf dem Bildschirm angezeigt
     * oder rechts an die zuvor gedrückte Ziffer angehängt angezeigt wird.
     * @param digit Die Ziffer, deren Taste gedrückt wurde
     */
    public void pressDigitKey(int digit) {
        if(digit > 9 || digit < 0) throw new IllegalArgumentException();

        if(screen.equals("0") || latestValue == Double.parseDouble(screen)) screen = "";

        screen = screen + digit;
    }

    /**
     * Empfängt den Befehl der C- bzw. CE-Taste (Clear bzw. Clear Entry).
     * Einmaliges Drücken der Taste löscht die zuvor eingegebenen Ziffern auf dem Bildschirm
     * so dass "0" angezeigt wird, jedoch ohne zuvor zwischengespeicherte Werte zu löschen.
     * Wird daraufhin noch einmal die Taste gedrückt, dann werden auch zwischengespeicherte
     * Werte sowie der aktuelle Operationsmodus zurückgesetzt, so dass der Rechner wieder
     * im Ursprungszustand ist.
     */
    /**
     * This clears on first press
    
     public void pressClearKey() {
        screen = "0";
        latestOperation = "";
        latestValue = 0.0;
    }
    */

    //Aufgabeteil 2d (testSingleClearKeepsMemory)
    private boolean ClearOnce = false;

    public void pressClearKey() {
        if (ClearOnce == false) {
            screen = "0";
            ClearOnce = !ClearOnce;
        } else {
            screen = "0";
            latestOperation = "";
            latestValue = 0.0;
            ClearOnce = false;
        }
    }
    /**
     * Empfängt den Wert einer gedrückten binären Operationstaste, also eine der vier Operationen
     * Addition, Substraktion, Division, oder Multiplikation, welche zwei Operanden benötigen.
     * Beim ersten Drücken der Taste wird der Bildschirminhalt nicht verändert, sondern nur der
     * Rechner in den passenden Operationsmodus versetzt.
     * Beim zweiten Drücken nach Eingabe einer weiteren Zahl wird direkt des aktuelle Zwischenergebnis
     * auf dem Bildschirm angezeigt. Falls hierbei eine Division durch Null auftritt, wird "Error" angezeigt.
     * @param operation "+" für Addition, "-" für Substraktion, "x" für Multiplikation, "/" für Division
     */
    public void pressBinaryOperationKey(String operation)  {
        latestValue = Double.parseDouble(screen);
        latestOperation = operation;
    }

    /**
     * Empfängt den Wert einer gedrückten unären Operationstaste, also eine der drei Operationen
     * Quadratwurzel, Prozent, Inversion, welche nur einen Operanden benötigen.
     * Beim Drücken der Taste wird direkt die Operation auf den aktuellen Zahlenwert angewendet und
     * der Bildschirminhalt mit dem Ergebnis aktualisiert.
     * @param operation "√" für Quadratwurzel, "%" für Prozent, "1/x" für Inversion
     */

    public void pressUnaryOperationKey(String operation) {
        latestValue = Double.parseDouble(screen);
        latestOperation = operation;
        var result = switch(operation) {
            case "√" -> Math.sqrt(Double.parseDouble(screen));
            case "%" -> Double.parseDouble(screen) / 100;
            case "1/x" -> 1 / Double.parseDouble(screen);
            default -> throw new IllegalArgumentException();
        };
        screen = Double.toString(result);
        if(screen.equals("NaN")) screen = "Error"; /** a NaN output should also show a "NaN" not "Error"*/
        if(screen.contains(".") && screen.length() > 11) screen = screen.substring(0, 10);
    }

    /**
     * Empfängt den Befehl der gedrückten Dezimaltrennzeichentaste, im Englischen üblicherweise "."
     * Fügt beim ersten Mal Drücken dem aktuellen Bildschirminhalt das Trennzeichen auf der rechten
     * Seite hinzu und aktualisiert den Bildschirm. Daraufhin eingegebene Zahlen werden rechts vom
     * Trennzeichen angegeben und daher als Dezimalziffern interpretiert.
     * Beim zweimaligem Drücken, oder wenn bereits ein Trennzeichen angezeigt wird, passiert nichts.
     */
    public void pressDotKey() {
        if(!screen.contains(".")) screen = screen + ".";
    }

    /**
     * Empfängt den Befehl der gedrückten Vorzeichenumkehrstaste ("+/-").
     * Zeigt der Bildschirm einen positiven Wert an, so wird ein "-" links angehängt, der Bildschirm
     * aktualisiert und die Inhalt fortan als negativ interpretiert.
     * Zeigt der Bildschirm bereits einen negativen Wert mit führendem Minus an, dann wird dieses
     * entfernt und der Inhalt fortan als positiv interpretiert.
     */
    public void pressNegativeKey() {
        screen = screen.startsWith("-") ? screen.substring(1) : "-" + screen;
    }

    /**
     * Empfängt den Befehl der gedrückten "="-Taste.
     * Wurde zuvor keine Operationstaste gedrückt, passiert nichts.
     * Wurde zuvor eine binäre Operationstaste gedrückt und zwei Operanden eingegeben, wird das
     * Ergebnis der Operation angezeigt. Falls hierbei eine Division durch Null auftritt, wird "Error" angezeigt.
     * Wird die Taste weitere Male gedrückt (ohne andere Tasten dazwischen), so wird die letzte
     * Operation (ggf. inklusive letztem Operand) erneut auf den aktuellen Bildschirminhalt angewandt
     * und das Ergebnis direkt angezeigt.
     */
    private double lastSecondOperand = 0.0;     // to repeat last operation with '='
    private boolean lastActionWasEquals = false; // track if '=' was just pressed

    public void pressEqualsKey() {
        ClearOnce = false;

        if (latestOperation.isEmpty()) { // if no operation, do nothing
            return;
        }

        double currentValue = Double.parseDouble(screen);
        double secondOperand;

        if (lastActionWasEquals) {
            // repeat last operation using stored second operand
            secondOperand = lastSecondOperand;
        } else {
            // first equals press: use the current screen as the second operand and store it
            secondOperand = currentValue;
            lastSecondOperand = secondOperand;
        }
        double result;
        switch (latestOperation) {
            case "+":
                result = latestValue + secondOperand;
                break;
            case "-":
                result = latestValue - secondOperand;
                break;
            case "x":
                result = latestValue * secondOperand;
                break;
            case "/":
                // explicit check for division by zero
                if (secondOperand == 0.0) {
                    screen = "Error";
                    // set state such that repeated '=' won't do anything harmful
                    lastActionWasEquals = false;
                return;
                } else {
                    result = latestValue / secondOperand;
                }
                break;
            default:
                throw new IllegalArgumentException();
        }

        if (Double.isNaN(result) || Double.isInfinite(result)) {
            screen = "Error";
            lastActionWasEquals = false;
            return;
        }

        // format and display result
        screen = formatResult(result);

        // update running state: the result becomes the new latestValue
        latestValue = Double.parseDouble(screen);
        lastActionWasEquals = true;
    }

    /**
     * Small helper to format the result similar to the original expectations:
     * - remove trailing ".0"
     * - cut off long fractional representations to 10 characters total if necessary
     */
    private String formatResult(double value) {
        String s = Double.toString(value);
        if (s.endsWith(".0")) {
            s = s.substring(0, s.length() - 2);
            return s;
        }
        // If it has a decimal and is too long, shorten to 10 chars (as original attempted)
        if (s.contains(".") && s.length() > 11) {
            s = s.substring(0, 10);
        }
        return s;
        }
    }

/**
 * (C): Resets everything on the first press,
 * Repeated equals ("="): The initial version did not repeat the last operation when "=" was pressed multiple times.
 * ("1/x"): Applying it to 0 resulted in "Infinity" instead of "Error".
 * Display formatting: Some results had unnecessary trailing ".0" or long decimal values beyond what a calculator normally shows.
 */