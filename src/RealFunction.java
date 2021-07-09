public interface RealFunction {
    double y(double x);
    RealFunction derivative();
    String text();

    default RealFunction comp(RealFunction that){
        return new Composition(this, that);
    }

    default RealFunction sum(RealFunction f){
        return new Sum(this, f);
    }

    default RealFunction product(RealFunction f){
        return new Product(this, f);
    }

    default RealFunction power(double e){
        return new Power(e).comp(this);
    }
}

class Functions {
    public final static RealFunction ZERO = new Constant(0);
    public final static RealFunction ONE = new Constant(1);
    public final static RealFunction MINUS = new Constant(-1);
    public final static RealFunction ID = new Identity();

    public final static RealFunction EXP = new RealFunction() {
        public double y(double x) { return Math.exp(x); }
        public RealFunction derivative() { return this; }
        public String text() { return "exp"; }
    };

    public final static RealFunction LOG = new RealFunction() {
        public double y(double x) { return Math.log(x); }
        public RealFunction derivative() { return ID.power(-1); }
        public String text() { return "log"; }
    };

    public final static RealFunction SINE = new RealFunction() {
        public double y(double x) { return Math.sin(x); }
        public RealFunction derivative() { return COSINE; }
        public String text() { return "sin"; }
    };

    public final static RealFunction COSINE = new RealFunction() {
        public double y(double x) { return Math.sin(x); }
        public RealFunction derivative() { return minus(SINE); }
        public String text() { return "cos"; }
    };

    public static RealFunction minus(RealFunction f){
        return new Product(MINUS, f);
    }
}

class Composition implements RealFunction {
    private final RealFunction f,g;

    public Composition(RealFunction f, RealFunction g){
        this.f = f;
        this.g = g;
    }

    @Override
    public double y(double x) {
        return f.y(g.y(x));
    }

    @Override
    public RealFunction derivative() {
        return new Product(
                f.derivative().comp(g),
                g.derivative());
    }

    @Override
    public String text() {
        return f.text() + "(" + g.text() + ")";
    }
}

class Constant implements RealFunction{

    private final double c;

    public Constant(double c){ this.c = c; }

    @Override
    public double y(double x) {
        return c;
    }

    @Override
    public RealFunction derivative() {
        return Functions.ZERO;
    }

    @Override
    public String text() {
        return Double.toString(c);
    }
}

class Identity implements RealFunction {
    @Override
    public double y(double x) {
        return x;
    }

    @Override
    public RealFunction derivative() {
        return Functions.ONE;
    }

    @Override
    public String text() {
        return "x";
    }
}

class Power implements RealFunction {
    private final double e;

    public Power(double e) {
        this.e = e;
    }

    @Override
    public double y(double x) {
        return Math.pow(x,e);
    }

    @Override
    public RealFunction derivative() {
        return new Product(new Constant(e), new Power(e - 1));
    }

    @Override
    public String text() {
       /* if(e == 1) return f.text();
        return "(" + f.text() + ")" + "^" + e;*/
        return "pow[" + e + "]";
    }
}

class Product implements RealFunction {
    private boolean isZero = false;
    private final RealFunction f,g;

    public Product(RealFunction f, RealFunction g){
        if(f == Functions.ZERO || g == Functions.ZERO)
            isZero = true;
        this.f = f;
        this.g = g;
    }

    @Override
    public double y(double x) {
        return isZero ? 0 : f.y(x) * g.y(x);
    }

    @Override
    public RealFunction derivative() {
        return isZero ? Functions.ZERO :
                new Sum(new Product(f.derivative(), g), new Product(f, g.derivative()));
    }

    @Override
    public String text() {
        return isZero? Functions.ZERO.text() : f.text() + "*" + g.text();
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

/*class Exponential implements RealFunction {
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
        return "log(" + f.text() + ")";
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
}*/

class Main {
    public static void main(String[] args){
        /*RealFunction polynom = new Sum(
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
        RealFunction logsin = new Logarithm(new Sine(Functions.ID));*/

        RealFunction deuxPointZero = Functions.ID.power(3).sum(Functions.LOG.comp(Functions.SINE.comp(Functions.ID)));
        RealFunction test = Functions.SINE.comp(Functions.COSINE.comp(Functions.LOG.comp(Functions.ID)));

        RealFunction function = test;
        System.out.println(function.text());
        RealFunction derivative = function.derivative(); //should be 4x + 1
        System.out.println(derivative.text());
        for(int i = 0; i < 10; ++i) System.out.println(derivative.y(i) +", ");
    }
}
