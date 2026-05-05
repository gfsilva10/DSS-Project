
/** ****************************************************************************** */
/** DISCLAIMER: Este código foi criado e alterado durante as aulas práticas */
/** de POO. Representa uma solução em construção, com base na matéria leccionada */
/** até ao momento da sua elaboração, e resulta da discussão e experimentação */
/** durante as aulas. Como tal, não deverá ser visto como uma solução canónica, */
/** ou mesmo acabada. É disponibilizado para auxiliar o processo de estudo. */
/** Os alunos são encorajados a testar adequadamente o código fornecido e a */
/** procurar soluções alternativas, à medida que forem adquirindo mais */
/** conhecimentos de POO. */
/** ****************************************************************************** */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Esta classe implementa um NewMenu em modo texto.
 *
 * @author José Creissac Campos
 * @version v3.2 (20201215)
 * @version v3.3 (20230502)
 */
public class NewMenu {

    // Interfaces auxiliares
    /**
     * Functional interface para handlers.
     */
    public interface Handler { // método de tratamento

        public void execute();
    }

    /**
     * Functional interface para pré-condições.
     */
    /**
     * Podia ser utilizado Predicate<T>
     */
    public interface PreCondition {

        public boolean validate();
    }

    // Varíável de classe para suportar leitura
    private static Scanner is = new Scanner(System.in);

    // Variáveis de instância
    private List<String> opcoes; // Lista de opções
    private List<PreCondition> disponivel; // Lista de pré-condições
    private List<Handler> handlers; // Lista de handlers

    // Construtor
    /**
     * Constructor for objects of class NewMenu
     */
    public NewMenu(String[] opcoes) {
        this.opcoes = Arrays.asList(opcoes);
        this.disponivel = new ArrayList<>();
        this.handlers = new ArrayList<>();
        this.opcoes.forEach(s -> {
            this.disponivel.add(() -> true);
            this.handlers.add(() -> System.out.println("\nAttention: Option not implemented!"));
        });
    }

    // Métodos de instância
    /**
     * Correr o NewMenu.
     *
     * Termina com Voltar (opcoes.size() + 1) ou Sair (opcoes.size() + 2).
     */
    public void run() {
        int op;
        do {
            show();
            op = readOption();
            // Opção Sair
            if (op == this.opcoes.size() + 2) {
                System.exit(0);
            }
            // Opção Voltar
            else if (op == this.opcoes.size() + 1) {
                break;
            }
            // testar pré-condição
            else if (op > 0 && !this.disponivel.get(op - 1).validate()) {
                System.out.println("Invalid Option! Try Again.");
            } else if (op > 0) {
                // executar handler
                this.handlers.get(op - 1).execute();
            }
        } while (true);
    }

    /**
     * Correr o NewMenu 1x.
     *
     * Termina com a opção 0 (zero).
     */
    public void runOnce() {
        int op;

        show();
        op = readOption();
        // Opção Sair
        if (op == this.opcoes.size() + 2) {
            System.exit(0);
        }
        // Opção Voltar - simplesmente retorna sem executar handler
        else if (op == this.opcoes.size() + 1) {
            return;
        }
        // testar pré-condição
        else if (op > 0 && !this.disponivel.get(op - 1).validate()) {
            System.out.println("Invalid Option! Try Again.");
        } else if (op > 0) {
            // executar handler
            this.handlers.get(op - 1).execute();
        }

    }

    /**
     * Método que regista uma uma pré-condição numa opção do NewMenu.
     *
     * @param i índice da opção (começa em 1)
     * @param b pré-condição a registar
     */
    public void setPreCondition(int i, PreCondition b) {
        this.disponivel.set(i - 1, b);
    }

    /**
     * Método para registar um handler numa opção do NewMenu.
     *
     * @param i indice da opção (começa em 1)
     * @param h handlers a registar
     */
    public void setHandler(int i, Handler h) {
        this.handlers.set(i - 1, h);
    }

    // Métodos auxiliares
    /**
     * Apresentar o NewMenu
     */
    private void show() {
        System.out.println("\n ======= McLei ======= \n");
        for (int i = 0; i < this.opcoes.size(); i++) {
            System.out.print(i + 1);
            System.out.print(" - ");
            System.out.println(this.disponivel.get(i).validate() ? this.opcoes.get(i) : "---");
        }
        System.out.println((this.opcoes.size() + 1) + " - Voltar");
        System.out.println((this.opcoes.size() + 2) + " - Sair\n");
    }

    /**
     * Ler uma opção válida
     */
    private int readOption() {
        int op;
        // Scanner is = new Scanner(System.in);

        System.out.print("Option: ");
        try {
            String line = is.nextLine();
            op = Integer.parseInt(line);
        } catch (NumberFormatException e) { // Não foi escrito um int
            op = -1;
        }
        if (op < 1 || op > this.opcoes.size() + 2) {
            System.out.println("Invalid Option!!!");
            op = -1;
        }
        return op;
    }
}
