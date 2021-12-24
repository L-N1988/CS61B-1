import static java.lang.Math.hypot;

public class Planet {
    public double xxPos;
    public double yyPos;
    public double xxVel;
    public double yyVel;
    public double mass;
    String imgFileName;
    static final double G = 6.67e-11;

    public Planet(double xP, double yP, double xV,
                  double yV, double m, String img) {
        xxPos = xP;
        yyPos = yP;
        xxVel = xV;
        yyVel = yV;
        mass = m;
        imgFileName = img;
    }

    public Planet(Planet p) {
        xxPos = p.xxPos;
        yyPos = p.yyPos;
        xxVel = p.xxVel;
        yyVel = p.yyVel;
        mass = p.mass;
        imgFileName = p.imgFileName;
    }

    public double calcDistance(Planet p) {
        return hypot(xxPos - p.xxPos, yyPos - p.yyPos);
    }

    public double calcForceExertedBy(Planet p) {
        double distance = this.calcDistance(p);
        return G * mass * p.mass / (distance * distance);
    }

    public double calcForceExertedByX(Planet p) {
        double force = this.calcForceExertedBy(p);
        double distance = this.calcDistance(p);
        return (p.xxPos - xxPos) * force / distance;
    }

    public double calcForceExertedByY(Planet p) {
        double force = this.calcForceExertedBy(p);
        double distance = this.calcDistance(p);
        return (p.yyPos - yyPos) * force / distance;
    }

    public double calcNetForceExertedByX(Planet[] plants) {
        double net_force = 0;
        for (Planet planet : plants) {
            if (!this.equals(planet)) {
                net_force += this.calcForceExertedByX(planet);
            }
        }
        return net_force;
    }

    public double calcNetForceExertedByY(Planet[] plants) {
        double net_force = 0;
        for (Planet planet : plants) {
            if (!this.equals(planet)) {
                net_force += this.calcForceExertedByY(planet);
            }
        }
        return net_force;
    }

    public void update(double time, double x_force, double y_force) {
        double acceleration_x = x_force / mass;
        double acceleration_y = y_force / mass;
        xxVel += acceleration_x * time;
        yyVel += acceleration_y * time;
        xxPos += xxVel * time;
        yyPos += yyVel * time;
    }
}