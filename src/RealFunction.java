public interface RealFunction {
    double y(double x);
    RealFunction derivative();
    String text();
}

/*interface ElementaryFunction extends RealFunction {
    RealFunction elementaryFunction();
    RealFunction insideFunction();

    @Override
    default RealFunction derivative() {
        return null;
    }
}*/

class Constant implements RealFunction{
    public final static RealFunction ZERO = new Constant(0);

    private final double c;

    public Constant(double c){ this.c = c; }

    @Override
    public double y(double x) {
        return c;
    }

    @Override
    public RealFunction derivative() {
        return ZERO;
    }

    @Override
    public String text() {
        return Double.toString(c);
    }
}

class Identity implements RealFunction {
    public static final RealFunction ONE = new Constant(1);

    @Override
    public double y(double x) {
        return x;
    }

    @Override
    public RealFunction derivative() {
        return ONE;
    }

    @Override
    public String text() {
        return "x";
    }
}

class Power implements RealFunction {
    private final double e;
    private final RealFunction f;

    public Power(RealFunction f, double e) {
        this.f = f;
        this.e = e;
    }

    @Override
    public double y(double x) {
        return Math.pow(f.y(x),e);
    }

    @Override
    public RealFunction derivative() {
        return new Product(f.derivative(),
                new Product(new Constant(e), new Power(f, e - 1)));
    }

    @Override
    public String text() {
        if(e == 1) return f.text();
        return "(" + f.text() + ")" + "^" + e;
    }
}

class Product implements RealFunction {
    private final RealFunction f,g;

    public Product(RealFunction f, RealFunction g){
        this.f = f;
        this.g = g;
    }

    @Override
    public double y(double x) {
        return f.y(x) * g.y(x);
    }

    @Override
    public RealFunction derivative() {
        return new Sum(new Product(f.derivative(), g), new Product(f, g.derivative()));
    }

    @Override
    public String text() {
        return f.text() + "*" + g.text();
    }
}

class Sum implements RealFunction {
    private final RealFunction f,g;

    public Sum(RealFunction f, RealFunction g){
        this.f = f;
        this.g = g;
    }

    @Override
    public double y(double x){
        return f.y(x) + g.y(x);
    }

    @Override
    public RealFunction derivative() {
        return new Sum(f.derivative(), g.derivative());
    }

    @Override
    public String text() {
        return f.text() + " + " + g.text();
    }
}

class Exponential implements RealFunction {
    private final RealFunction f;

    Exponential(RealFunction f) {
        this.f = f;
    }

    @Override
    public double y(double x) {
        return Math.exp(f.y(x));
    }

    @Override
    public RealFunction derivative() {
        return new Product(f.derivative(), this);
    }

    @Override
    public String text() {
        return "exp(" + f.text() + ")";
    }
}

class Logarithm implements RealFunction {
    private final RealFunction f;

    Logarithm(RealFunction f) {
        this.f = f;
    }

    @Override
    public double y(double x) {
        return Math.log(f.y(x));
    }

    @Override
    public RealFunction derivative() {
        return new Product(f.derivative(), new Power(f, -1));
    }

    @Override
    public String text() {
        return "exp(" + f.text() + ")";
    }
}

class Sine implements RealFunction {
    private final RealFunction f;

    Sine(RealFunction f) {
        this.f = f;
    }

    @Override
    public double y(double x) {
        return Math.sin(f.y(x));
    }

    @Override
    public RealFunction derivative() {
        return new Product(f.derivative(), new Cosine(f));
    }

    @Override
    public String text() {
        return "sin(" + f.text() + ")";
    }
}

class Cosine implements RealFunction {
    private final RealFunction f;

    Cosine(RealFunction f) {
        this.f = f;
    }

    @Override
    public double y(double x) {
        return Math.cos(f.y(x));
    }

    @Override
    public RealFunction derivative() {
        return new Product( f.derivative(),
                new Product(new Constant(-1), new Sine(f)));
    }

    @Override
    public String text() {
        return "cos(" + f.text() + ")";
    }
}

class Main {
    public static void main(String[] args){
        RealFunction polynom = new Sum(
                new Product(new Constant(2), new Power(new Identity(), 2)), // 2x^2
                new Identity() //x
        );
        RealFunction tangeant = new Product(new Sine(new Identity()),
                new Power(new Cosine(new Identity()), -1));
        RealFunction bordel = new Power(
                new Sum(
                        new Product(new Constant(2), new Power(new Identity(), 4)),
                        new Exponential(new Product(new Constant(-1),
                                new Sum(new Product(new Constant(4), new Identity()), new Constant(3))))),
                0.6d);

        RealFunction function = bordel;
        System.out.println(function.text());
        RealFunction derivative = function.derivative(); //should be 4x + 1
        System.out.println(derivative.text());
        for(int i = 0; i < 10; ++i) System.out.println(derivative.y(i) +", ");
    }
}
