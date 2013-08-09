#ifndef DM_GAMEOBJECT_SCRIPT_UTIL_H
#define DM_GAMEOBJECT_SCRIPT_UTIL_H

namespace dmGameObject
{
    bool LoadModules(dmResource::HFactory factory, dmScript::HContext script_context, lua_State* L, dmLuaDDF::LuaModule* lua_module);
}

#endif // #ifndef DM_GAMEOBJECT_SCRIPT_UTIL_H
