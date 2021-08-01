import java.text.DecimalFormat;

public interface RealFunction {
    double y(double x);
    RealFunction derivative();
    String text();

    default RealFunction optimised(){
        return this;
    }

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
    public final static DecimalFormat DF = new DecimalFormat("#.####");

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
        public RealFunction derivative() { return new Power(-1); }
        public String text() { return "log"; }
    };

    public final static RealFunction SINE = new RealFunction() {
        public double y(double x) { return Math.sin(x); }
        public RealFunction derivative() { return COSINE; }
        public String text() { return "sin"; }
    };

    public final static RealFunction COSINE = new RealFunction() {
        public double y(double x) { return Math.cos(x); }
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
    public RealFunction optimised() {
        return f.optimised().comp(g.optimised());
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
    public RealFunction optimised() {
        if(c - Math.rint(c) != 0) return this;
        switch((int)c) {
            case 1 : return Functions.ONE;
            case 0 : return Functions.ZERO;
            case -1 : return Functions.MINUS;
            default : return this;
        }
    }

    @Override
    public String text() {
        return Functions.DF.format(c);
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
    public RealFunction optimised() {
        return this;
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
    public RealFunction optimised() {
        if(e == 1) return Functions.ID;
        if(e == 0) return Functions.ONE;
        return this;
    }

    @Override
    public String text() {
        if (e == 1) return "";
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
    public RealFunction optimised() {
        RealFunction fo = f.optimised();
        RealFunction go = g.optimised();

        if(fo.equals(Functions.ZERO) || go.equals(Functions.ZERO)) return Functions.ZERO;
        if(fo.equals(Functions.ONE)) return go;
        if(go.equals(Functions.ONE)) return fo;

        if(f instanceof Constant && g instanceof Constant)
            return new Constant(f.y(0) * g.y(0));

        return fo.product(go);
    }

    @Override
    public String text() {
        if(f.equals(Functions.ONE))
            return g.text();
        if(g.equals(Functions.ONE))
            return f.text();
        if(f.equals(Functions.MINUS))
            return "-" + g.text();
        if(g.equals(Functions.MINUS))
            return "-" + f.text();
        return isZero ? Functions.ZERO.text() : f.text() + " * " + g.text();
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
    public RealFunction optimised() {
        RealFunction fo = f.optimised();
        RealFunction go = g.optimised();

        if(fo instanceof Constant && go instanceof Constant)
            return new Constant(fo.y(0) + go.y(0));

        if(fo.equals(Functions.ZERO)) return go;
        if(go.equals(Functions.ZERO)) return fo;

        return fo.sum(go);
    }

    @Override
    public String text() {
        return f.text() + "  +  " + g.text();
    }
}

class Main {
    public static void main(String[] args){
        RealFunction deuxPointZero = Functions.ID.power(3).sum(Functions.LOG.comp(Functions.SINE.comp(Functions.ID)));
        RealFunction test = Functions.SINE.comp(Functions.COSINE.comp(Functions.LOG.comp(Functions.ID)));
        RealFunction tangeant = (Functions.COSINE.power(-1).product(Functions.SINE));
        RealFunction three = Functions.SINE.power(2)
                            .product(Functions.COSINE.comp(Functions.ID.power(2)));

        RealFunction function = three;
        System.out.println("Function : " + function.text());
        for(int i = 0; i < 2; ++i){
            function = function.derivative();
        }
        System.out.println("4th derivative : " + function.text());
        function = function.optimised();
        System.out.println("optimised : " + function.text());
//        System.out.println(function.text());
//        RealFunction derivative = function.derivative(); //should be 4x + 1
//        System.out.println(derivative.text());
//        for(int i = 0; i < 10; ++i) System.out.println(derivative.y(i) +", ");
    }
}

class Notes {
    public static void main(String[] args){
        int points = 438;
        System.out.println("Note : " + grade(points) + " / 6");
    }

    static double grade(int points) {
        double rawGrade = 0.875 + 5.25 * (points / 500d);
        System.out.println(rawGrade);
        return Math.rint(rawGrade * 4) / 4;
    }
}
