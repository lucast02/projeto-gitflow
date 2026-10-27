
public class Calculadora {

    public static int somar(int a, int b) {
        return a + b;
    }

    public static int subtrair(int a, int b) {
        return a - b;
    }
    public static int multiplicar(int a, int b) {
    return a * b;
}

    public static void main(String[] args) {
        System.out.println("Projeto Gitflow");
        System.out.println("Soma: " + somar(10, 5));
        System.out.println("Subtracao: " + subtrair(10, 5));
        System.out.println("Multiplicacao: " + multiplicar(10, 5));
    }
}
