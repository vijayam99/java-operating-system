public class HelloWorld extends UserlandProcess {
    @Override
    public void main() {
        while (true) {
            System.out.println("Hello World");
            cooperate();
            // Slow output so console is readable
            try { Thread.sleep(50); } catch (Exception e) { }
        }
    }
}