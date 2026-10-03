import json
import pytest
from app.cli import load_scenario, main


def test_import_accepts_standalone_android_schema_and_bom(tmp_path):
    path=tmp_path / "scenario.json"
    path.write_text(json.dumps({"id":"exported","name":"Payment","description":"","preconditions":[],
        "steps":[{"action":"TAP","target":"PAY","value":None,"description":None}],
        "assertions":[{"type":"ASSERT_API_STATUS","target":"/payment","expected":"401"}]}),encoding="utf-8-sig")
    assert load_scenario(path).steps[0].action.value == "TAP"
    assert main(["validate",str(path)]) == 0


def test_import_rejects_report_wrapper_and_unknown_action(tmp_path):
    path=tmp_path / "bad.json"
    for value in ({"report":{}},{"id":"x","name":"x","steps":[{"action":"SHELL"}]}):
        path.write_text(json.dumps(value),encoding="utf-8")
        assert main(["validate",str(path)]) == 1


def test_import_bounds_file_size(tmp_path):
    path=tmp_path / "oversize.json"
    path.write_bytes(b" " * (1024*1024+1))
    with pytest.raises(ValueError,match="1 MiB"):
        load_scenario(path)
