package com.accor.wcp.flow;

import com.accor.kengine.KengineLog;
import com.accor.kengine.KengineLogStore;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Data;

@Deprecated
@Data
public class KengineLogs implements KengineLogStore {

  private List<KengineLog> logs = new CopyOnWriteArrayList<>();
}
