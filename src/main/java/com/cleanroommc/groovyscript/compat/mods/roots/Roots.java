package com.cleanroommc.groovyscript.compat.mods.roots;

import com.cleanroommc.groovyscript.api.IGameObjectHandler;
import com.cleanroommc.groovyscript.api.Result;
import com.cleanroommc.groovyscript.compat.mods.ModPropertyContainer;
import com.cleanroommc.groovyscript.gameobjects.GameObjectHandlerManager;
import com.cleanroommc.groovyscript.gameobjects.GameObjectHandlers;
import epicsquid.roots.init.HerbRegistry;
import epicsquid.roots.modifiers.CostType;
import epicsquid.roots.modifiers.Modifier;
import epicsquid.roots.modifiers.ModifierRegistry;
import epicsquid.roots.ritual.RitualRegistry;
import epicsquid.roots.spell.SpellBase;
import epicsquid.roots.spell.SpellRegistry;
import net.minecraft.util.ResourceLocation;

public class Roots extends ModPropertyContainer {

    public final AnimalHarvest animalHarvest = new AnimalHarvest();
    public final AnimalHarvestFish animalHarvestFish = new AnimalHarvestFish();
    public final BarkCarving barkCarving = new BarkCarving();
    public final Chrysopoeia chrysopoeia = new Chrysopoeia();
    public final FeyCrafter feyCrafter = new FeyCrafter();
    public final FlowerGeneration flowerGeneration = new FlowerGeneration();
    public final LifeEssence lifeEssence = new LifeEssence();
    public final Modifiers modifiers = new Modifiers();
    public final Moss moss = new Moss();
    public final Mortar mortar = new Mortar();
    public final Pacifist pacifist = new Pacifist();
    public final Predicates predicates = new Predicates();
    public final Pyre pyre = new Pyre();
    public final Rituals rituals = new Rituals();
    public final RunicShearBlock runicShearBlock = new RunicShearBlock();
    public final RunicShearEntity runicShearEntity = new RunicShearEntity();
    public final Spells spells = new Spells();
    public final SummonCreature summonCreature = new SummonCreature();
    public final Transmutation transmutation = new Transmutation();


    public Roots() {
        addRegistry(animalHarvest);
        addRegistry(animalHarvestFish);
        addRegistry(barkCarving);
        addRegistry(chrysopoeia);
        addRegistry(feyCrafter);
        addRegistry(flowerGeneration);
        addRegistry(lifeEssence);
        addRegistry(modifiers);
        addRegistry(moss);
        addRegistry(mortar);
        addRegistry(pacifist);
        addRegistry(pyre);
        addRegistry(predicates);
        addRegistry(rituals);
        addRegistry(runicShearBlock);
        addRegistry(runicShearEntity);
        addRegistry(spells);
        addRegistry(summonCreature);
        addRegistry(transmutation);
    }

    @Override
    public void initialize() {
        GameObjectHandlerManager.registerGameObjectHandler("roots", "ritual", IGameObjectHandler.wrapStringGetter(RitualRegistry::getRitual));
        GameObjectHandlerManager.registerGameObjectHandler("roots", "herb", IGameObjectHandler.wrapStringGetter(HerbRegistry::getHerbByName));
        GameObjectHandlerManager.registerGameObjectHandler("roots", "cost", IGameObjectHandler.wrapEnum(CostType.class, false));
        GameObjectHandlerManager.registerGameObjectHandler("roots", "spell", Roots::getSpell);
        GameObjectHandlerManager.registerGameObjectHandler("roots", "modifier", Roots::getModifier);
    }

    private static Result<SpellBase> getSpell(String s, Object... args) {
        if (s.contains(":")) {
            Result<ResourceLocation> rl = GameObjectHandlers.parseResourceLocation(s, args);
            if (rl.hasError()) return Result.error(rl.getError());
            SpellBase spell = SpellRegistry.getSpell(rl.getValue());
            return spell == null ? Result.error() : Result.some(spell);
        }
        if (!s.startsWith("spell_")) {
            s = "spell_" + s;
        }
        SpellBase spell = SpellRegistry.getSpell(s);
        return spell == null ? Result.error() : Result.some(spell);
    }

    private static Result<Modifier> getModifier(String s, Object... args) {
        Result<ResourceLocation> rl = GameObjectHandlers.parseResourceLocation(s, args);
        if (rl.hasError()) return Result.error(rl.getError());
        Modifier modifier = ModifierRegistry.get(rl.getValue());
        return modifier == null ? Result.error() : Result.some(modifier);
    }
}
