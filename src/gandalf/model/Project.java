package gandalf.model;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.Integer.parseInt;
import jasonlib.IO;
import jasonlib.Json;
import jasonlib.Log;
import jasonlib.OS;
import java.io.File;
import java.util.List;
import com.google.common.collect.Lists;

public class Project {

  public static final File projectsDir = new File(OS.getAppFolder("gandalf"), "projects");
  private static int idCounter = 0;
  {
    projectsDir.mkdirs();

    for (File file : projectsDir.listFiles()) {
      int id = parseInt(file.getName());
      idCounter = Math.max(idCounter, id + 1);
    }
  }

  public static final String DEFAULT_NAME = "New Project";

  public int id;
  public String name;
  public final List<GFile> files = Lists.newArrayList();

  public Project() {
    this(idCounter++, DEFAULT_NAME);

    files.add(new GFile("Main.java", IO.from(getClass(), "main").toString()));
  }

  public Project(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public GFile getFile(String name) {
    for (GFile file : files) {
      if (file.name.equals(name)) {
        return file;
      }
    }
    return null;
  }

  public void save() {
    File dir = new File(projectsDir, id + "");
    Log.debug("Saving project to " + dir);

    if (dir.exists()) {
      for (File child : dir.listFiles()) {
        child.delete();
      }
    } else {
      dir.mkdirs();
    }

    Json json = Json.object()
        .with("id", id)
        .with("name", name);
    IO.from(json).to(new File(dir, "project.json"));

    for (GFile file : files) {
      IO.from(file.getContent()).to(new File(dir, file.name));
    }
  }

  public static void delete(int id) {
    File dir = new File(projectsDir, id + "");
    Log.info("Deleting project " + id);

    for (File child : dir.listFiles()) {
      child.delete();
    }

    dir.delete();
  }

  public static Project load(int id) {
    File dir = new File(projectsDir, id + "");
    checkState(dir.exists());

    Json json = IO.from(dir, "project.json").toJson();

    Project ret = new Project(json.getInt("id"), json.get("name"));

    for (File file : dir.listFiles()) {
      if (!file.getName().equals("project.json")) {
        ret.files.add(new GFile(file.getName(), IO.from(file).toString()));
      }
    }

    return ret;
  }

}
