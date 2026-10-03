"""Import sanitized TestScenario files through the same existing runner API."""
import argparse
import json
import time
from pathlib import Path
from urllib.request import Request, urlopen
from urllib.parse import urlparse
from app.models.scenario import TestScenario


def load_scenario(path):
    source=Path(path)
    if source.stat().st_size > 1024 * 1024:
        raise ValueError("Scenario file exceeds 1 MiB.")
    return TestScenario.model_validate_json(source.read_text(encoding="utf-8-sig"))


def main(argv=None):
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument("command",choices=("validate","run"))
    parser.add_argument("scenario")
    parser.add_argument("--runner",default="http://127.0.0.1:8000")
    parser.add_argument("--allow-mock",action="store_true")
    args=parser.parse_args(argv)
    try:
        scenario=load_scenario(args.scenario)
        if args.command == "validate":
            print("Valid TestScenario: schema matches the Repro Runner contract.")
            return 0
        url=urlparse(args.runner)
        if url.scheme not in ("http","https") or not url.hostname or url.username or url.password or url.query or url.fragment:
            raise ValueError("Use a trusted runner HTTP URL without credentials, query or fragment.")
        base=args.runner.rstrip("/")
        def request(route,payload=None):
            body=json.dumps(payload).encode() if payload is not None else None
            with urlopen(Request(base+route,data=body,headers={"Content-Type":"application/json"}),timeout=25) as response:
                return json.load(response)
        readiness=request("/preflight",scenario.model_dump(mode="json"))
        if not readiness["ready"]:
            raise ValueError("; ".join(readiness["issues"]))
        if readiness["executionMode"] == "MOCK" and not args.allow_mock:
            raise ValueError("Mock runner enabled. Pass --allow-mock explicitly or restart in ADB mode.")
        print("Execution mode:",readiness["executionMode"])
        result=request("/execute",scenario.model_dump(mode="json"))
        deadline=time.monotonic()+190
        while result["status"] in ("PENDING","RUNNING"):
            if time.monotonic() >= deadline:
                request(f"/executions/{result['execution_id']}/cancel",{})
                raise ValueError("Execution timed out and cancellation was requested.")
            time.sleep(.75)
            result=request(f"/executions/{result['execution_id']}")
        print("Execution:",result["execution_id"],result["status"])
        print("This is the runner status; product outcome still requires scoped observed evidence.")
        return 0 if result["status"] in ("PASSED","FAILED") else 1
    except Exception as error:
        if isinstance(error,ValueError) and not hasattr(error,"errors"):
            print("Import/run stopped:",str(error))
        else:
            print("Import/run stopped: invalid file, unavailable runner or incomplete response. Check configuration and retry.")
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
