import static spark.Spark.*;

import models.Hero;
import models.Squad;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class App {
    static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 4567; //return default port if heroku-port isn't set (i.e. on localhost)
    }
    public static void main(String[] args) {

                    port(getHerokuAssignedPort());
                    staticFileLocation("/public");


        get("/", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            int totalHeroes = Hero.getHeroRegistry().size();
            int totalSquads = Squad.getAllSquads().size();
            int squadlessHeroes = 0;
            int squadfullHeroes = 0;
            for (Hero hero : Hero.getHeroRegistry()) {
                if (hero.getSquadAlliance().equals("")) {
                    squadlessHeroes += 1;
                } else {
                    squadfullHeroes += 1;
                }
            }
            model.put("totalHeroes", totalHeroes);
            model.put("totalSquads", totalSquads);
            model.put("squadlessHeroes", squadlessHeroes);
            model.put("squadfullHeroes", squadfullHeroes);
            model.put("uniqueId", request.session().attribute("uniqueId"));
            return new ModelAndView(model, "index.hbs");
        }, new HandlebarsTemplateEngine());


        post("/success", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            String uniqueId = request.queryParams("uniqueId");
            request.session().attribute("uniqueId", uniqueId);
            model.put("uniqueId", uniqueId);
            return new ModelAndView(model, "success.hbs");
        }, new HandlebarsTemplateEngine());


        get("/heroes/new", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("uniqueId", request.session().attribute("uniqueId"));
            return new ModelAndView(model, "hero-form.hbs");
        }, new HandlebarsTemplateEngine());


        post("/heroes/new", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            String name = request.queryParams("name");
            int age = Integer.parseInt(request.queryParams("age"));
            String power = request.queryParams("power");
            String weakness = request.queryParams("weakness");
            Hero newHero = new Hero(name, age, power, weakness);
            return new ModelAndView(model, "success.hbs");
        }, new HandlebarsTemplateEngine());


        get("/squads/new", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            List<Hero> squadlessHeroes = new ArrayList<>();
            for (Hero hero : Hero.getHeroRegistry()) {
                if (hero.getSquadAlliance().equals("")) {
                    squadlessHeroes.add(hero);
                }
            }
            model.put("squadlessHeroes", squadlessHeroes);
            return new ModelAndView(model, "squad-form.hbs");
        }, new HandlebarsTemplateEngine());


        post("/squads/new", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            List<Hero> squadlessHeroes = new ArrayList<>();
            for (Hero hero : Hero.getHeroRegistry()) {
                if (hero.getSquadAlliance().equals("")) {
                    squadlessHeroes.add(hero);
                }
            }

            String name = request.queryParams("name");
            String cause = request.queryParams("cause");
            String heroName = request.queryParams("founder");
            Hero squadFounder = null;
            for (Hero hero : squadlessHeroes) {
                if (hero.getName().equalsIgnoreCase(heroName)) {
                    squadFounder = hero;
                    break;
                }
            }
            assert squadFounder != null;
            Squad newSquad = new Squad(name, cause, squadFounder);
            return new ModelAndView(model, "success.hbs");
        }, new HandlebarsTemplateEngine());


        get("/heros", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("allHeroes", Hero.getHeroRegistry());
            model.put("allSquads", Squad.getAllSquads());
            model.put("uniqueId", request.session().attribute("uniqueId"));
            return new ModelAndView(model, "heros-squads.hbs");
        }, new HandlebarsTemplateEngine());


        get("/heroes/:id", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            int itemId = Integer.parseInt(request.params(":id"));
            Hero foundHero = Hero.findHero(itemId);
            model.put("hero", foundHero);
            model.put("uniqueId", request.session().attribute("uniqueId"));
            return new ModelAndView(model, "hero-details.hbs");
        }, new HandlebarsTemplateEngine());


        get("/squads/:id", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            int itemId = Integer.parseInt(request.params(":id"));
            Squad foundSquad = Squad.findSquad(itemId);
            model.put("squad", foundSquad);
            model.put("uniqueId", request.session().attribute("uniqueId"));
            return new ModelAndView(model, "squad-details.hbs");
        }, new HandlebarsTemplateEngine());


        post("/heroes/:id/update", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            int itemId = Integer.parseInt(request.params(":id"));
            Hero updateHero = Hero.findHero(itemId);
            updateHero.updateName(request.queryParams("name"));
            updateHero.updateAge(Integer.parseInt(request.queryParams("age")));
            updateHero.updatePower(request.queryParams("power"));
            updateHero.updateWeakness(request.queryParams("weakness"));
            return new ModelAndView(model, "success.hbs");
        }, new HandlebarsTemplateEngine());


        get("/heros/:id/update", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            int itemId = Integer.parseInt(request.params(":id"));
            Hero updateHero = Hero.findHero(itemId);
            model.put("updateHero", updateHero);
            model.put("uniqueId", request.session().attribute("uniqueId"));
            return new ModelAndView(model, "hero-form.hbs");
        }, new HandlebarsTemplateEngine());


        get("/heros/:id/remove", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            int itemId = Integer.parseInt(request.params(":id"));
            Hero.deleteHero(itemId);
            model.put("uniqueId", request.session().attribute("uniqueId"));
            return new ModelAndView(model, "heros-squads.hbs");
        }, new HandlebarsTemplateEngine());


        post("/squads/:id/update", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            int itemId = Integer.parseInt(request.params(":id"));
            Squad foundSquad = Squad.findSquad(itemId);
            String heroName = request.queryParams("addHero");
            Hero heroToAdd = null;
            for (Hero hero : Hero.getHeroRegistry()) {
                if (hero.getName().equalsIgnoreCase(heroName)) {
                    heroToAdd = hero;
                    break;
                }
            }
            foundSquad.changeHeroSquad(heroToAdd, foundSquad);
            model.put("uniqueId", request.session().attribute("uniqueId"));
            return new ModelAndView(model, "success.hbs");
        }, new HandlebarsTemplateEngine());


        get("/squads/:id/update", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            int itemId = Integer.parseInt(request.params(":id"));
            Squad foundSquad = Squad.findSquad(itemId);
            List<Hero> nonMembers = new ArrayList<>();
            for (Hero hero : Hero.getHeroRegistry()) {
                if (!hero.getSquadAlliance().equalsIgnoreCase(foundSquad.getName())) {
                    nonMembers.add(hero);
                }
            }
            model.put("nonMembers", nonMembers);
            model.put("squad", foundSquad);
            model.put("uniqueId", request.session().attribute("uniqueId"));
            return new ModelAndView(model, "update-form.hbs");
        }, new HandlebarsTemplateEngine());


        post("/squads/:id/remove", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            int itemId = Integer.parseInt(request.params(":id"));
            Squad foundSquad = Squad.findSquad(itemId);
            String heroName = request.queryParams("removeHero");
            Hero heroToRemove = null;
            for (Hero hero : Hero.getHeroRegistry()) {
                if (hero.getName().equalsIgnoreCase(heroName)) {
                    heroToRemove = hero;
                    break;
                }
            }
            foundSquad.removeMember(heroToRemove);
            model.put("uniqueId", request.session().attribute("uniqueId"));
            return new ModelAndView(model, "success.hbs");
        }, new HandlebarsTemplateEngine());


        get("/squads/:id/remove", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            int itemId = Integer.parseInt(request.params(":id"));
            Squad foundSquad = Squad.findSquad(itemId);
            model.put("members", foundSquad.getMembers());
            model.put("squad", foundSquad);
            model.put("uniqueId", request.session().attribute("uniqueId"));
            return new ModelAndView(model, "update-form.hbs");
        }, new HandlebarsTemplateEngine());

    }
}
